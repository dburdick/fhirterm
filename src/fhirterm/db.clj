(ns fhirterm.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]
            [honeysql.core :as honeysql]))

(def ^:dynamic *db* nil)

(defn start [{config :db}]
  (alter-var-root #'*db*
                  (constantly config))
  config)

(defn stop []
  (alter-var-root #'*db*
                  (constantly nil)))

(defn- extract-db-from-args [args]
  (let [maybe-db (first args)
        others (rest args)]
    (if (and (map? maybe-db)
             (set/superset? (set (keys maybe-db)) #{:subname :subprotocol :classname}))
      [maybe-db others] ;; yep, it's db
      [*db* args])))

(defn- query-to-sql-vector [query]
  (cond
   (string? query) [query]
   (vector? query) query
   (map? query) (honeysql/format query)))

(defn- db-and-query-from-args [args]
  (let [[db [query]] (extract-db-from-args args)
        sql-vector (query-to-sql-vector query)]
    [db sql-vector]))

(defn q [& args]
  (let [[db sql-vector] (db-and-query-from-args args)]
    (println "SQL: " (pr-str sql-vector))
    (jdbc/query db sql-vector)))

(defn e! [& args]
  (let [[db sql-vector] (db-and-query-from-args args)]
    (jdbc/execute! db sql-vector)))

(defn i! [db & args]
  (apply jdbc/insert! db args))

(defn q-one [& args]
  (first (apply q args)))

(defn q-val [& args]
  (first (vals (apply q-one args))))
