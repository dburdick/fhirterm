(ns fhirterm.tasks.import-snomed
  (:require [fhirterm.db :as db]
            [honeysql.helpers :as sql]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as sh]
            [fhirterm.csv :as csv]
            [fhirterm.tasks.util :refer :all]))

(def snomed-tables
  {:snomed_concepts  [[:id "BIGINT"]
                      [:effective_time "INTEGER"]
                      [:active "BOOLEAN"]
                      [:module_id "BIGINT"]
                      [:definition_status_id "BIGINT"]]

   :snomed_ancestors_descendants [[:concept_id "BIGINT PRIMARY KEY"]
                                  [:ancestors "BIGINT[]"]
                                  [:descendants "BIGINT[]"]]

   :snomed_descriptions [[:id "BIGINT"]
                         [:effective_time "INTEGER"]
                         [:active "BOOLEAN"]
                         [:module_id "BIGINT"]
                         [:concept_id "BIGINT"]
                         [:language_code "VARCHAR"]
                         [:type_id "BIGINT"]
                         [:term "TEXT"]
                         [:case_significance_id "BIGINT"]]

   :snomed_relations [[:id "BIGINT"]
                      [:effective_time "INTEGER"]
                      [:active "BOOLEAN"]
                      [:module_id "BIGINT"]
                      [:source_id "BIGINT"]
                      [:destination_id "BIGINT"]
                      [:relationship_group "BIGINT"]
                      [:type_id "BIGINT"]
                      [:characteristic_type_id "BIGINT"]
                      [:modifier_id "BIGINT"]]

   :snomed_is_a_relations [[:id "BIGINT"]
                           [:source_id "BIGINT"]
                           [:destination_id "BIGINT"]]})

(def snomed-indices
  ["CREATE INDEX snomed_concepts_on_id_idx ON snomed_concepts(id)"
   "CREATE INDEX snomed_relations_on_id_idx ON snomed_relations(id)"
   "CREATE INDEX snomed_relations_on_type_id_idx ON snomed_relations(type_id)"
   "CREATE INDEX snomed_relations_on_source_id_idx ON snomed_relations(source_id)"
   "CREATE INDEX snomed_relations_on_destination_id_idx ON snomed_relations(destination_id)"
   "CREATE INDEX snomed_is_a_relations_on_source_id_idx ON snomed_is_a_relations(source_id)"
   "CREATE INDEX snomed_is_a_relations_on_destination_id_idx ON snomed_is_a_relations(destination_id)"
   "CREATE INDEX snomed_descriptions_on_id_idx ON snomed_descriptions(id)"
   "CREATE INDEX snomed_descriptions_on_concept_id_idx ON snomed_descriptions(concept_id)"])

(def stored-procedures
  ["CREATE OR REPLACE FUNCTION snomed_get_descendants(root bigint)
RETURNS bigint[] AS
$BODY$
  WITH RECURSIVE t(source_id) AS (
  SELECT source_id FROM snomed_is_a_relations
  WHERE destination_id = root

  UNION

  SELECT sr.source_id FROM snomed_is_a_relations AS sr
  JOIN t ON t.source_id = sr.destination_id
) SELECT array_agg(source_id) FROM t;
$BODY$
LANGUAGE sql IMMUTABLE"

   "CREATE OR REPLACE FUNCTION snomed_get_ancestors(root bigint)
RETURNS bigint[] AS
$BODY$
WITH RECURSIVE t(destination_id) AS (
  SELECT destination_id FROM snomed_is_a_relations
  WHERE source_id = root

  UNION

  SELECT sr.destination_id FROM snomed_is_a_relations AS sr
  JOIN t ON t.destination_id = sr.source_id

) SELECT array_agg(destination_id) FROM t;
$BODY$
LANGUAGE sql IMMUTABLE"])

(defn- prepare-db [db]
  (jdbc/with-db-transaction [trans db]
    (doseq [[tbl tbl-columns] snomed-tables]
      (db/e! (format "DROP TABLE IF EXISTS %s" (name tbl)))
      (db/e! trans
             (apply jdbc/create-table-ddl tbl tbl-columns)))

    (doseq [i snomed-indices]
      (db/e! trans i))

    (doseq [i stored-procedures]
      (db/e! trans i)))

  (println "Created SNOMED tables"))

(defn- find-file [file-seq re]
  (let [result (first (filter (fn [f]
                                (re-find re (.getPath f)))
                              file-seq))]
    (when result
      (.getPath result))))

(defn- load-snomed-csv [db tmp-path]
  (let [files-seq (file-seq (io/file tmp-path))
        concepts-path (find-file files-seq
                                 #"SnomedCT_Release_INT_\d{8}/RF2Release/Full/Terminology/sct2_Concept_Full_INT_\d{8}.txt$")
        relations-path (find-file files-seq
                                  #"SnomedCT_Release_INT_\d{8}/RF2Release/Full/Terminology/sct2_Relationship_Full_INT_\d{8}.txt$")
        descriptions-path (find-file files-seq
                                     #"SnomedCT_Release_INT_\d{8}/RF2Release/Full/Terminology/sct2_Description_Full-en_INT_\d{8}.txt$")]

    (jdbc/with-db-transaction [trans db]
      (println (format "Importing SNOMED concepts from %s" concepts-path))
      (db/e! (format "COPY snomed_concepts FROM '%s' WITH DELIMITER AS E'\t' CSV HEADER QUOTE AS '`'"
                     concepts-path)))

    (jdbc/with-db-transaction [trans db]
      (println (format "Importing SNOMED descriptions from %s" descriptions-path))
      (db/e! (format "COPY snomed_descriptions FROM '%s' WITH DELIMITER AS E'\t' CSV HEADER QUOTE AS '`'"
                     descriptions-path)))

    (jdbc/with-db-transaction [trans db]
      (println (format "Importing SNOMED relations from %s" relations-path))
      (db/e! (format "COPY snomed_relations FROM '%s' WITH DELIMITER AS E'\t' CSV HEADER QUOTE AS '`'"
                     relations-path)))

    (println "Copying \"is-a\" relations into separate table...")
    (db/e! "INSERT INTO snomed_is_a_relations (id, source_id, destination_id)
            SELECT id, source_id, destination_id FROM snomed_relations
            WHERE type_id = 116680003 AND active = TRUE")

    (println "Finished importing SNOMED")))

(defn- prewalk-is-a-relations [db]
  (println "Prewalking SNOMED graph (may take some time)")
  (jdbc/db-do-commands db false "VACUUM ANALYZE snomed_is_a_relations")
  (db/e! "INSERT INTO snomed_ancestors_descendants (concept_id, ancestors, descendants)
          SELECT t.id, snomed_get_ancestors(t.id), snomed_get_descendants(t.id)
          FROM
          (SELECT DISTINCT(source_id) AS id FROM snomed_is_a_relations
           UNION
           SELECT DISTINCT(destination_id) AS id FROM snomed_is_a_relations) t")
  (println "Finished prewalking SNOMED graph"))

(defn perform* [db zip-file]
  (unzip-file zip-file
              (fn [tmp-path]
                (prepare-db db)
                (load-snomed-csv db tmp-path)
                (prewalk-is-a-relations db))))

(defn perform [db args]
  (let [zip-file (first args)]
    (check-zip-file-is-specified zip-file "SnomedCT_Release_INT_XXXXXXXX.zip")
    (perform* db zip-file)))
