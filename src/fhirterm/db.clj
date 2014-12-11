(ns fhirterm.db
  (:require [clojure.java.jdbc :as jdbc]
            [sqlingvo.db :as sqlingvo-db]))

(defn start [{config :db}]
  (let [db-constructor (ns-resolve 'sqlingvo.db (symbol (:subprotocol config)))]
    (db-constructor config)))

(defn e! [db sql]
  (jdbc/execute! db sql))

(defn i! [& args]
  (apply jdbc/insert! args))

(defn stop []
  ;; do nothing for now
  )
