(ns fhirterm.tasks.import-loinc
  (:require [fhirterm.db :as db]
            [sqlingvo.core :as sql]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as sh]
            [fhirterm.csv :as csv]
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

(defn- prepare-db [db]
  (jdbc/with-db-transaction [trans db]
    (db/e! trans
           (apply jdbc/create-table-ddl loinc-table loinc-columns))

    (db/e! trans
           (format "CREATE INDEX loinc_loincs_on_order_obs_idx ON %s(order_obs)"
                   (name loinc-table))))

  (println (format "Created %s table" (name loinc-table))))

(defn- load-loinc-csv [db csv-path]
  (let [columns (map first loinc-columns)]
    (jdbc/with-db-transaction [trans db]
      (println "Uploading...")
      (csv/read-file csv-path
                     {:skip-first-row true}
                     (fn [row]
                       (db/i! trans loinc-table columns row))))

    (let [loincs-count (db/q-one db (sql/select [(sql/as '(count :*) :count)]
                                      (sql/from loinc-table)))]
      (println (format "Done, imported %d LOINC records" (:count loincs-count))))))

(defn- perform* [db zip-path]
  (let [tmp-path (.getPath (mk-tmp-dir!))
        unzip-result (sh/sh "unzip" zip-path "-d" tmp-path)]
    (if (not= 0 (:exit unzip-result))
      (exit (str "Cannot unzip archive. Do you have unzip utility installed?\n"
                 "Additional information: " (pr-str unzip-result)) 1)

      (do
        (println "Unzipped successfuly")
        (prepare-db db)
        (load-loinc-csv db (str/join "/" [tmp-path "loinc.csv"]))))

    (sh/sh "rm" "-rf" tmp-path)
    (println "Temp directory removed")))

(defn perform [db args]
  (let [zip-file (first args)]
    (if (not zip-file)
      (exit "You have to specify path to downloaded LOINC_XXX_Text.zip file\n"
            "Example: lein task import-loinc ~/Downloads/LOINC_248_Text.zip"
            1)

      (if (not (.canRead (io/file zip-file)))
        (exit (format "File %s is not readable!" zip-file) 1)
        (perform* db zip-file)))))
