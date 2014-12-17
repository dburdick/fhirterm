(ns fhirterm.fhir.value-set
  (:require [fhirterm.db :as db]
            [honeysql.helpers :as sql]
            [fhirterm.json :as json]
            [fhirterm.naming-system.core :as naming-system]))

(defn find-by-id [db id]
  (let [result (db/q-one db (-> (sql/select :content)
                                (sql/from :fhir_value_sets)
                                (sql/where [:= :id id])))]

    (if result
      (json/parse (:content result))
      nil)))

(defn expand [db vs]
  (let [includes (get-in vs [:compose :include])
        ns-and-filters (reduce (fn [acc [s fs]]
                                 (assoc acc s (map :filter fs)))
                               {} (group-by :system includes))]
    (map (fn [[ns filters]]
           (naming-system/filter-codes db ns filters))
         ns-and-filters)))
