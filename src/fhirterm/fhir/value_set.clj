(ns fhirterm.fhir.value-set
  (:require [fhirterm.db :as db]
            [honeysql.helpers :as sql]
            [fhirterm.json :as json]
            [fhirterm.util :as util]
            [fhirterm.naming-system.core :as naming-system]
            [clj-time.core :as time]
            [clojure.string :as str]))

(def expansion-cache (atom {}))
(def enable-cache false)

(defn find-by-id [db id]
  (let [result (db/q-one db (-> (sql/select :content)
                                (sql/from :fhir_value_sets)
                                (sql/where [:= :id id])))]

    (if result
      (json/parse (:content result))
      nil)))

(defn- filters-from-include [inc]
  (-> (map :filter inc)
      (into (vector (map (fn [cs] {:property "code" :value cs :op "in"})
                         (map (fn [i] (map :code (:concept i))) inc))))))

(defn- expand* [db vs]
  (let [includes (get-in vs [:compose :include])
        ns-and-filters (reduce (fn [acc [s fs]]
                                 (assoc acc s (filter (complement nil?)
                                                      (filters-from-include fs))))
                               {} (group-by :system includes))]

    (println "!!!! expanding by =>" (pr-str ns-and-filters))
    (reduce (fn [res [ns filters]]
              (into res (naming-system/filter-codes db ns filters)))
            [] ns-and-filters)))

(defn- apply-coding-filters [codings params]
  (let [filter-str (:filter params)]
    (if (and filter-str (not (str/blank? filter-str)))
      ;; perform filtering
      (let [filter-str (str/lower-case filter-str)
            filter-fn (fn [^clojure.lang.PersistentArrayMap c]
                        (let [^String sv (:search-vector c)]
                          (or (nil? sv) (>= (.indexOf sv filter-str) 0))))]
        (filter filter-fn codings))

      ;; otherwise, just return all codings
      codings)))

(defn expand [db vs params]
  (let [result (if (and enable-cache
                        (contains? @expansion-cache (:identifier vs)))

                 (get @expansion-cache (:identifier vs))
                 (let [r (expand* db vs)]
                   (swap! expansion-cache (fn [c]
                                            (assoc c (:identifier vs) r)))
                   r))

        filtered-result (apply-coding-filters result params)]

    (assoc vs :expansion {:identifier (util/uuid)
                          :timestamp (time/now)
                          :contains (map (fn [x] (dissoc x :search-vector))
                                         filtered-result)})))
