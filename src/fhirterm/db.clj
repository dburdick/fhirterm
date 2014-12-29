(ns fhirterm.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]
            [clojure.string :as str]
            [clj-time.core :as t]
            [clojure.string :as str]
            [clj-time.coerce :as tc]
            [fhirterm.json :as json]
            [honeysql.core :as honeysql])
  (:import (org.joda.time DateTime)
           (java.sql Timestamp)
           (java.util Date)
           (org.postgresql.jdbc4 Jdbc4Array)
           (org.postgresql.util PGobject)))

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

(defmulti to-jdbc class)
(defmulti from-jdbc class)

(defn- query-to-sql-vector [something]
  (let [[query & args] (cond
                        (string? something) [something]
                        (vector? something) something
                        (map? something) (honeysql/format something))]
    ;; perform coercing on args
    (into [query] (map to-jdbc args))))

(defn- db-and-query-from-args [args]
  (let [[db [query]] (extract-db-from-args args)
        sql-vector (query-to-sql-vector query)]
    [db sql-vector]))

(defn q [& args]
  (let [[db sql-vector] (db-and-query-from-args args)
        result (report-actual-sql-error
                (jdbc/query db sql-vector))]
    (map from-jdbc result)))

(defn e! [& args]
  (let [[db sql-vector] (db-and-query-from-args args)]
    (report-actual-sql-error
     (jdbc/execute! db sql-vector))))

(defn i! [& args]
  (let [[db [tbl & args]] (extract-db-from-args args)]
    (cond
     ;; first arg is a map, so each arg is a map
     (map? (first args))
     (report-actual-sql-error
      (apply jdbc/insert! db tbl (map to-jdbc args)))

     ;; first arg is vector of columns,
     ;; and rest of args is vectors of values
     (vector? (first args))
     (apply jdbc/insert! db tbl (first args) (map to-jdbc (rest args)))

     :else
     (throw (IllegalArgumentException. "Incorrect arguments of db/i! fn")))))

(defn q-one [& args]
  (first (apply q args)))

(defn q-val [& args]
  (first (vals (apply q-one args))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; COERCING
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn pg-obj [type value]
  (let [coerced-value (condp = type
                        :jsonb (json/generate value)
                        :json (json/generate value)
                        (str value))
        pg-obj (org.postgresql.util.PGobject.)]
    (.setType pg-obj (name type))
    (.setValue pg-obj coerced-value)

    pg-obj))

(def db-timezone (t/time-zone-for-id "UTC"))

(defn- sql-time-to-clj-time [sql-time]
  "Returns a DateTime instance in the Application Time Zone
   corresponding to the given java.sql.Timestamp object."

  (t/to-time-zone (tc/from-sql-time sql-time)
                  db-timezone))

(defn- clj-time-to-sql-time [clj-time]
  "Convert `clj-time` to a java.sql.Timestamp instance performing
   timezone conversion to UTC."
  (tc/to-sql-time (t/to-time-zone
                   clj-time
                   db-timezone)))

(defn- quote-seq [v]
  "Returns PostreSQL literal representation of sequence"
  (str "{" (str/join "," (map #(str "\"" % "\"") v)) "}"))

(defn- map-map [m map-fn]
  (reduce (fn [new-map [k v]]
            (assoc new-map k (map-fn v)))
          {} m))


(defmethod to-jdbc clojure.lang.PersistentArrayMap [m] (map-map m to-jdbc))
(defmethod to-jdbc clojure.lang.PersistentHashMap [m] (map-map m to-jdbc))

(defmethod to-jdbc clojure.lang.Keyword [v]
  (name v))

(defmethod to-jdbc org.joda.time.DateTime [v]
  (clj-time-to-sql-time v))

(defmethod to-jdbc java.util.Date [v]
  (java.sql.Timestamp. (.getTime v)))

(defmethod to-jdbc clojure.lang.PersistentVector [v]
  (quote-seq v))

(defmethod to-jdbc clojure.lang.PersistentList [v]
  (quote-seq v))

(defmethod to-jdbc clojure.lang.PersistentHashSet [s]
  (quote-seq s))

(defmethod to-jdbc :default [v] v)

(defmethod from-jdbc clojure.lang.PersistentArrayMap [m] (map-map m from-jdbc))
(defmethod from-jdbc clojure.lang.PersistentHashMap [m] (map-map m from-jdbc))

(defmethod from-jdbc org.postgresql.util.PGobject [v]
  (if (= (.getType v) "json")
    (json/parse (.toString v))
    (.toString v)))

(defmethod from-jdbc org.postgresql.jdbc4.Jdbc4Array [v]
  (vec (.getArray v)))

(defmethod from-jdbc java.sql.Timestamp [v]
  (sql-time-to-clj-time v))

(defmethod from-jdbc :default [v] v)
