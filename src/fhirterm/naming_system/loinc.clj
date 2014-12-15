(ns fhirterm.naming-system.loinc
  (:require [sqlingvo.core :as sql]
            [fhirterm.db :as db]))

(defn lookup [db params]
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
