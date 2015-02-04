(ns fhirterm.tasks-test
  (:require [fhirterm.system :as system]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [fhirterm.db :as db]
            [fhirterm.tasks.import-vs :as import-vs]
            [fhirterm.tasks.import-snomed :as import-snomed]
            [fhirterm.tasks.import-loinc :as import-loinc]
            [fhirterm.tasks.import-rxnorm :as import-rxnorm]))

(def snomed-zip-path "data/snomed.zip")
(def loinc-zip-path "data/loinc.zip")
(def rxnorm-zip-path "data/rxnorm.zip")

(deftest ^:task import-vs-test
  (import-vs/perform db/*db* ["test/fixtures/value_sets"]))

(deftest ^:task import-snomed-test
  (import-snomed/perform db/*db* [snomed-zip-path])

  (is (> (db/q-val "SELECT COUNT(*) FROM snomed_descriptions") 0))
  (is (> (db/q-val "SELECT COUNT(*) FROM snomed_ancestors_descendants") 0)))

(deftest ^:task import-rxnorm-test
  (import-rxnorm/perform db/*db* [rxnorm-zip-path])
  (is (> (db/q-val "SELECT COUNT(*) FROM rxn_conso") 0)))

(deftest ^:task import-loinc-test
  (import-loinc/perform db/*db* [loinc-zip-path])

  (is (> (db/q-val "SELECT COUNT(*) FROM loinc_loincs") 0)))

;; Generally, we need to run task tests in specific order
;; so we define this hook fn to specify order here
(defn test-ns-hook []
  (system/start-headless (system/read-config "test/config.json"))
  (import-vs-test)
  (import-loinc-test)
  (import-snomed-test)
  (import-rxnorm-test)
  (system/stop))
