(ns fhirterm.db
  (:require [clojure.java.jdbc :as jdbc]))

(defn start [config]
  ;; just return db config, cause it's also JDBC connection
  (:db config))

(defn stop []
  ;; do nothing for now
  )
