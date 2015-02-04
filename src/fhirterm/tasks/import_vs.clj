(ns fhirterm.tasks.import-vs
  (:require [fhirterm.json :as json]
            [clj-time.format :as tf]
            [clojure.string :as str]
            [fhirterm.fhir.client :as fhir-client]))

(def date-formatter (tf/formatter "yyyy-MM-dd"))

(defn- import-value-sets [valuesets]
  (doseq [vs valuesets]
    (when (not (fhir-client/resource-exists? "ValueSet" (:id vs)))
      (fhir-client/create-resource "ValueSet" vs)
      (println "Imported" (:id vs)))))

(defn perform [_ [dir]]
  (println "Importing FHIR ValueSets from" dir
           "to" (fhir-client/get-description))

  (let [files (filter #(.endsWith (.getPath %) ".json")
                      (file-seq (clojure.java.io/file dir)))

        jsons (map #(json/parse (slurp %)) files)]
    (import-value-sets jsons)))
