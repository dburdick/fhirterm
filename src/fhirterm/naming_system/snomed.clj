(ns fhirterm.naming-system.snomed
  (:require [honeysql.helpers :as sql]
            [clojure.string :as str]
            [fhirterm.db :as db]))

(def snomed-uri "http://snomed.info/sct")

(defn lookup-code [db params])

(defn filter-codes [db filters]
  (println "!!!!" (pr-str filters)))
