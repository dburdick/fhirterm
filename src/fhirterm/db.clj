(ns fhirterm.db
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as honeysql]))

(defn start [{config :db}]
  config)

(defn stop []
  ;; do nothing for now
  )

(defn e! [db sql]
  (jdbc/execute! db [sql]))

(defn i! [& args]
  (apply jdbc/insert! args))

(defn q* [db & args]
  (println "[SQL]" (pr-str (first args)))
  (apply jdbc/query db args))

(defmacro q [db & query]
  `(q* ~db (honeysql/format
            ~@query)))

(defmacro q-one [db & query]
  `(first (q ~db ~@query)))
