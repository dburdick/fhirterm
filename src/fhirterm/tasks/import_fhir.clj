(ns fhirterm.tasks.import-fhir
  (:require [fhirterm.db :as db]
            [org.httpkit.client :as http]
            [fhirterm.json :as json]
            [clj-time.format :as tf]
            [clojure.string :as str]
            [clj-time.coerce :as tc]
            [fhirterm.fhir.client :as fhir-client]))

(def vs-url "http://www.hl7.org/implement/standards/FHIR-Develop/valuesets.json")

(def date-formatter (tf/formatter "yyyy-MM-dd"))

(defn- import-value-sets [valuesets]
  (doseq [vs valuesets]
    (when (not (fhir-client/resource-exists? "ValueSet" (:id vs)))
      (fhir-client/create-resource "ValueSet" vs)
      (println "Imported" (:id vs)))))

(defn- parse-json-bundle [body]
  (let [vs (map :resource (:entry (json/parse body)))]
    (println (format "%d ValueSets in the Bundle" (count vs)))
    vs))

(defn perform [db args]
  (println "Importing FHIR ValueSets from" vs-url
           "to" (fhir-client/get-description))

  (let [{:keys [body status headers]} @(http/get vs-url)]
    (println (format "HTTP status: %d, bytes read: %d"
                     status (count body)))

    (if (= status 200)
      (import-value-sets (parse-json-bundle body))
      (println "HTTP error, terminating."))))
