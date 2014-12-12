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

(defn q* [db & args]
  (apply jdbc/query db args))

(defn- set-db-as-first-argument [db query]
  (map (fn [q] (cons (first q) (into (rest q) (list db))))
       query))

(defmacro q [db & query]
  `(q* ~db (sqlingvo.core/sql ~@(set-db-as-first-argument db query))))

(defn stop []
  ;; do nothing for now
  )
