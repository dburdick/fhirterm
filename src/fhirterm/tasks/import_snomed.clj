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
  {:snomed_concepts  [[:id "UNSIGNED BIG INT"]
                      [:effective_time "INTEGER"]
                      [:active "BOOLEAN"]
                      [:module_id "UNSIGNED BIG INT"]
                      [:definition_status_id "UNSIGNED BIG INT"]]

   :snomed_descriptions [[:id "UNSIGNED BIG INT"]
                         [:effective_time "INTEGER"]
                         [:active "BOOLEAN"]
                         [:module_id "UNSIGNED BIG INT"]
                         [:concept_id "UNSIGNED BIG INT"]
                         [:language_code "VARCHAR"]
                         [:type_id "UNSIGNED BIG INT"]
                         [:term "TEXT"]
                         [:case_significance_id "UNSIGNED BIG INT"]]

   :snomed_relations [[:id "UNSIGNED BIG INT"]
                      [:effective_time "INTEGER"]
                      [:active "BOOLEAN"]
                      [:module_id "UNSIGNED BIG INT"]
                      [:source_id "UNSIGNED BIG INT"]
                      [:destination_id "UNSIGNED BIG INT"]
                      [:relationship_group "UNSIGNED BIG INT"]
                      [:type_id "UNSIGNED BIG INT"]
                      [:characteristic_type_id "UNSIGNED BIG INT"]
                      [:modifier_id "UNSIGNED BIG INT"]]})

(def snomed-indices ["CREATE INDEX snomed_concepts_on_id_idx ON snomed_concepts(id)"
                     "CREATE INDEX snomed_relations_on_id_idx ON snomed_relations(id)"
                     "CREATE INDEX snomed_relations_on_type_id_idx ON snomed_relations(type_id)"
                     "CREATE INDEX snomed_descriptions_on_id_idx ON snomed_descriptions(id)"
                     "CREATE INDEX snomed_descriptions_on_concept_id_idx ON snomed_descriptions(concept_id)"])

(defn- prepare-db [db]
  (jdbc/with-db-transaction [trans db]
    (doseq [[tbl tbl-columns] snomed-tables]
      (db/e! trans
             (apply jdbc/create-table-ddl tbl tbl-columns)))

    (doseq [i snomed-indices]
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
        csv-options {:skip-first-row true :separator \tab :quote \`}
        concepts-columns (map first (:snomed_concepts snomed-tables))
        descriptions-columns (map first (:snomed_descriptions snomed-tables))
        relations-columns (map first (:snomed_relations snomed-tables))

        concepts-path (find-file files-seq
                                 #"SnomedCT_Release_INT_\d{8}/RF2Release/Full/Terminology/sct2_Concept_Full_INT_\d{8}.txt$")
        relations-path (find-file files-seq
                                  #"SnomedCT_Release_INT_\d{8}/RF2Release/Full/Terminology/sct2_Relationship_Full_INT_\d{8}.txt$")
        descriptions-path (find-file files-seq
                                     #"SnomedCT_Release_INT_\d{8}/RF2Release/Full/Terminology/sct2_Description_Full-en_INT_\d{8}.txt$")]

    (jdbc/with-db-transaction [trans db]
      (println (format "Importing SNOMED concepts from %s" concepts-path))
      (csv/read-file concepts-path csv-options
                     (fn [row]
                       (db/i! trans :snomed_concepts concepts-columns row))))

    (jdbc/with-db-transaction [trans db]
      (println (format "Importing SNOMED descriptions from %s" descriptions-path))
      (csv/read-file descriptions-path csv-options
                     (fn [row]
                       (db/i! trans :snomed_descriptions descriptions-columns row))))

    (jdbc/with-db-transaction [trans db]
      (println (format "Importing SNOMED relations from %s" relations-path))
      (csv/read-file relations-path csv-options
                     (fn [row]
                       (db/i! trans :snomed_relations relations-columns row))))

    (println "Finished importing SNOMED")))

(defn perform* [db zip-file]
  (unzip-file zip-file
                (fn [tmp-path]
                  (prepare-db db)
                  (load-snomed-csv db tmp-path))))

(defn perform [db args]
  (let [zip-file (first args)]
    (check-zip-file-is-specified zip-file "SnomedCT_Release_INT_XXXXXXXX.zip")
    (perform* db zip-file)))
