(ns fhirterm.tasks.import-loinc
  (:require [fhirterm.db :as db]
            [honeysql.helpers :as sql]
            [clojure.java.jdbc :as jdbc]
            [fhirterm.tasks.util :refer :all]))

(def loinc-columns
  [[:loinc_num "varchar primary key"] [:component "varchar"] [:property "varchar"]
   [:time_aspect "varchar"] [:system "varchar"] [:scale_type "varchar"]
   [:method_type "varchar"] [:class "varchar"] [:source "varchar"]
   [:date_last_changed "integer"] [:change_type "varchar"] [:comments "text"]
   [:status "varchar"] [:consumer_name "varchar"] [:molar_mass "varchar"]
   [:classtype "integer"] [:formula "varchar"] [:species "varchar"]
   [:example_answers "text"] [:acssym "text"] [:base_name "varchar"]
   [:naaccr_id "varchar"] [:code_table "varchar"] [:survey_quest_text "text"]
   [:survey_quest_src "varchar"] [:units_required "varchar"]
   [:submitted_units "varchar"] [:relatednames2 "text"] [:shortname "varchar"]
   [:order_obs "varchar"] [:cdisc_common_tests "varchar"]
   [:hl7_field_subfield_id "varchar"] [:external_copyright_notice "text"]
   [:example_units "varchar"] [:long_common_name "varchar"]
   [:hl7_v2_datatype "varchar"] [:hl7_v3_datatype "varchar"]
   [:curated_range_and_units "text"] [:document_section "varchar"]
   [:example_ucum_units "varchar"] [:example_si_ucum_units "varchar"]
   [:status_reason "varchar"] [:status_text "text"] [:change_reason_public "text"]
   [:common_test_rank "integer"] [:common_order_rank "integer"]
   [:common_si_test_rank "integer"] [:hl7_attachment_structure "varchar"]])

(def loinc-table :loinc_loincs)
(def indices ["CREATE INDEX loinc_loincs_on_order_obs_idx ON %s(order_obs)"
              "CREATE INDEX loinc_loincs_on_status_idx ON %s(status)"])

(defn- prepare-db [db]
  (jdbc/with-db-transaction [trans db]
    (db/e! trans (format "DROP TABLE IF EXISTS %s" (name loinc-table)))
    (db/e! trans
           (apply jdbc/create-table-ddl loinc-table loinc-columns))

    (doseq [i indices]
      (db/e! trans
             (format i (name loinc-table)))))

  (println (format "Created %s table" (name loinc-table))))

(defn- load-loinc-csv [db csv-path]
  (println "Uploading LOINC CSV")

  (db/e! (format "COPY loinc_loincs FROM '%s' WITH DELIMITER AS ',' CSV HEADER QUOTE AS '\"'"
                 csv-path))

  (let [loincs-count (db/q-val db (-> (sql/select [:%count.* :count])
                                      (sql/from loinc-table)))]
    (println (format "Done, imported %d LOINC records" loincs-count))))

(defn- perform* [db zip-path]
  (unzip-file zip-path
              (fn [tmp-path]
                (prepare-db db)
                (load-loinc-csv db (make-path tmp-path "loinc.csv")))))

(defn perform [db args]
  (let [zip-file (first args)]
    (check-zip-file-is-specified zip-file "LOINC_XXX_Text.zip")
    (perform* db zip-file)))
