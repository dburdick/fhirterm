(ns fhirterm.tasks.import-valuesets
  (:require [fhirterm.db :as db]
            [org.httpkit.client :as http]
            [clojure.java.jdbc :as jdbc]
            [fhirterm.json :as json]
            [clj-time.format :as tf]
            [clojure.string :as str]
            [clj-time.coerce :as tc])
  (:import java.sql.Timestamp
           org.postgresql.util.PGobject))

(def vs-url "http://www.hl7.org/implement/standards/FHIR-Develop/valuesets.json")

(def date-formatter (tf/formatter "yyyy-MM-dd"))

(defn pg-json [json]
  (let [pg-object (org.postgresql.util.PGobject.)]
    (.setType pg-object "jsonb")
    (.setValue pg-object (json/generate json))
    pg-object))

(def table-columns
  [[:id "varchar primary key"]
   [:identifier "varchar"]
   [:version "varchar"]
   [:date "date"]
   [:content "jsonb"]])

(defn- create-value-sets-table [db]
  (jdbc/with-db-transaction [trans db]
    (db/e! trans "DROP TABLE IF EXISTS fhir_value_sets")
    (db/e! trans
           (apply jdbc/create-table-ddl :fhir_value_sets table-columns))

    (db/e! trans "CREATE UNIQUE INDEX fhir_value_sets_on_identifier_idx ON fhir_value_sets(identifier)")))

(defn- insert-value-sets [db valuesets]
  (let [rows (map (fn [{:keys [id identifier version date] :as vs}]
                    (let [fixed-date (str/replace (or date "") #"T.+$" "")]
                      {:id id
                       :identifier identifier
                       :version version
                       :date (if (not (str/blank? fixed-date))
                               (tc/to-sql-time (tf/parse date-formatter fixed-date))
                               nil)
                       :content (pg-json vs)}))
                  valuesets)]

    (jdbc/with-db-transaction [trans db]
      (doseq [row rows]
        (db/i! trans "fhir_value_sets" row)))

    (println (format "Inserted %d ValueSets" (count rows)))))

(defn- parse-json-bundle [body]
  (let [vs (map :resource (:entry (json/parse body)))]
    (println (format "%d ValueSets in the Bundle" (count vs)))
    vs))

(defn perform [db args]
  (println "Importing FHIR ValueSets from" vs-url)

  (create-value-sets-table db)

  (let [{:keys [body status headers]} @(http/get vs-url)]
    (println (format "HTTP status: %d, bytes read: %d"
                     status (count body)))

    (if (= status 200)
      (insert-value-sets db (parse-json-bundle body))
      (println "HTTP error, terminating."))))
