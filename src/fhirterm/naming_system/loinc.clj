(ns fhirterm.naming-system.loinc
  (:require [sqlingvo.core :as sql]
            [clojure.string :as str]
            [fhirterm.db :as db]))

(def loinc-uri "http://loinc.org")

(defn lookup-code [db params]
  (let [found-loinc (db/q-one db (sql/select [*]
                                   (sql/from :loinc_loincs)
                                   (sql/where `(= :loinc_num ~(:code params)))
                                   (sql/limit 1)))]
    (when found-loinc
      {:name "LOINC"
       :version "to.do"
       :abstract false
       :display (:shortname found-loinc)
       :designation [{:value (:shortname found-loinc)}
                     {:value (:long_common_name found-loinc)}]})))

(defn- filter-to-sql-cond [f]
  `(= ~(keyword (str/lower-case (:property f))) ~(:value f)))

(defn- filters-to-sql-cond [filters]
  `(or ~@(map (fn [fs]
                `(and ~@(map filter-to-sql-cond fs)))
              filters)))

(defn- row-to-coding [row]
  {:system loinc-uri
   :abstract false
   :version "to.do"
   :code (:loinc_num row)
   :display (:shortname row)})

(defn filter-codes [db filters]
  (let [codings (db/q db (sql/select [:loinc_num :shortname]
                           (sql/from :loinc_loincs)
                           (sql/where (filters-to-sql-cond filters))))]
    (map row-to-coding codings)))
