(ns fhirterm.tasks-test
  (:require [fhirterm.system :as system]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [fhirterm.db :as db]
            [fhirterm.tasks.import-fhir :as import-fhir]
            [fhirterm.tasks.import-snomed :as import-snomed]
            [fhirterm.tasks.import-loinc :as import-loinc]))

(def snomed-zip-path "data/snomed.zip")
(def loinc-zip-path "data/loinc.zip")

(deftest ^:task import-fhir-test
  (import-fhir/perform db/*db* []))

(deftest ^:task import-snomed-test
  (import-snomed/perform db/*db* [snomed-zip-path])

  (is (> (db/q-val "SELECT COUNT(*) FROM snomed_descriptions") 0))
  (is (> (db/q-val "SELECT COUNT(*) FROM snomed_ancestors_descendants") 0)))

(deftest ^:task import-loinc-test
  (import-loinc/perform db/*db* [loinc-zip-path])

  (is (> (db/q-val "SELECT COUNT(*) FROM loinc_loincs") 0)))

;; Generally, we need to run task tests in specific order
;; so we define this hook fn to specify order here
(defn test-ns-hook []
  (system/start-headless (system/read-config "test/config.json"))
  (import-fhir-test)
  (import-loinc-test)
  (import-snomed-test)
  (system/stop))
