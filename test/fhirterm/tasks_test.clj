(ns fhirterm.tasks-test
  (:require [fhirterm.system :as system]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [fhirterm.db :as db]
            [fhirterm.tasks.import-valuesets :as import-vs]
            [fhirterm.tasks.import-snomed :as import-snomed]
            [fhirterm.tasks.import-loinc :as import-loinc]))

(def snomed-zip-path "data/SnomedCT_Release_INT_20140731.zip")
(def loinc-zip-path "data/LOINC_248_Text.zip")

(deftest ^:task import-vs-test
  (let [output (with-out-str (import-vs/perform db/*db* []))
        checks [#"HTTP status: 200"
                #"Inserted \d+ ValueSets"]]
    (println output)

    (doseq [c checks]
      (is (re-find c output)))))

(deftest ^:task import-snomed-test
  (let [output (with-out-str (import-snomed/perform db/*db* [snomed-zip-path]))
        checks [#"Created SNOMED tables"
                #"Finished importing SNOMED"
                #"Temporary directory removed"]]
    (println output)

    (doseq [c checks]
      (is (re-find c output)))))

(deftest ^:task import-loinc-test
  (let [output (with-out-str (import-loinc/perform db/*db* [loinc-zip-path]))
        checks [#"Done, imported \d+ LOINC records"
                #"Temporary directory removed"]]
    (println output)

    (doseq [c checks]
      (is (re-find c output)))))

;; Generally, we need to run task tests in specific order
;; so we define this hook fn to specify order here
(defn test-ns-hook []
  (system/start-headless (system/read-config "test/config.json"))
  (import-vs-test)
  (import-loinc-test)
  (import-snomed-test)
  (system/stop))
