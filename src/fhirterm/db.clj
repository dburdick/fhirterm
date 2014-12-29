(ns fhirterm.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]
            [clojure.string :as str]
            [honeysql.core :as honeysql]))

(def ^:dynamic *db* nil)

(defmacro report-actual-sql-error [& body]
  `(try
     ~@body
     (catch java.sql.SQLException e#
       (if (.getNextException e#) ;; rethrow exception containing SQL error
         (let [msg# (.getMessage (.getNextException e#))]
           (throw (java.sql.SQLException.
                   (str (str/replace (.getMessage e#)
                                     "Call getNextException to see the cause." "")
                        "\n" msg#))))
         (throw e#)))))

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
  (report-actual-sql-error
   (let [[db sql-vector] (db-and-query-from-args args)]
     (jdbc/query db sql-vector))))

(defn e! [& args]
  (report-actual-sql-error
   (let [[db sql-vector] (db-and-query-from-args args)]
     (jdbc/execute! db sql-vector))))

(defn i! [db & args]
  (report-actual-sql-error
   (apply jdbc/insert! db args)))

(defn q-one [& args]
  (first (apply q args)))

(defn q-val [& args]
  (first (vals (apply q-one args))))
