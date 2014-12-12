(ns fhirterm.tasks.import-valuesets
  (:require [fhirterm.db :as db]
            [org.httpkit.client :as http]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [fhirterm.json :as json]))

(def vs-url "http://www.hl7.org/implement/standards/FHIR-Develop/valuesets.json")

(defn- insert-value-sets [db valuesets]
  (let [rows (map (fn [vs]
                    {:id      (:id vs)
                     :identifier (:identifier vs)
                     :version (:version vs)
                     :date    (str/replace (or (:date vs) "") #"-" "")
                     :content (json/generate vs)})
                  valuesets)]

    (jdbc/with-db-transaction [trans db]
      (db/e! trans ["DELETE FROM fhir_value_sets"])

      (doseq [row rows]
        (db/i! trans "fhir_value_sets" row)))

    (println (format "Inserted %d ValueSets" (count rows)))))

(defn- parse-json-bundle [body]
  (let [vs (map :resource (:entry (json/parse body)))]
    (println (format "%d ValueSets in the Bundle" (count vs)))
    vs))

(defn perform [db args]
  (println "Importing FHIR ValueSets from" vs-url)

  (let [{:keys [body status headers]} @(http/get vs-url)]
    (println (format "HTTP status: %d, bytes read: %d"
                     status (count body)))

    (if (= status 200)
      (insert-value-sets db (parse-json-bundle body))
      (println "HTTP error, terminating."))))
