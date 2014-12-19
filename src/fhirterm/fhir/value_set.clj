(ns fhirterm.fhir.value-set
  (:require [fhirterm.db :as db]
            [honeysql.helpers :as sql]
            [fhirterm.json :as json]
            [fhirterm.util :as util]
            [fhirterm.naming-system.core :as naming-system]
            [clj-time.core :as time]
            [clojure.string :as str]))

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

(defn- expand-with-compose-include [expansion db vs]
  (let [includes (get-in vs [:compose :include])
        ns-and-filters (reduce (fn [acc [s fs]]
                                 (assoc acc s (filter (complement nil?)
                                                      (filters-from-include fs))))
                               {} (group-by :system includes))]

    (reduce (fn [res [ns filters]]
              (into res (naming-system/filter-codes db ns filters)))
            expansion ns-and-filters)))

(defn- expand-with-define [expansion db {{:keys [system concept]} :define :as vs}]
  (reduce (fn reduce-fn [result c]
            (let [result (conj result
                               {:code    (:code c)
                                :display (:display c)
                                :system  system})
                  inner-concept (:concept c)]
              (if inner-concept
                (into result (reduce reduce-fn [] inner-concept))
                result)))
          expansion
          concept))

(defn- apply-expansion-filters [codings params]
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

(defn- expand-with-compose-exclude [expansion db vs]
  ;; todo!!
  expansion)

(defn- expand* [db vs params]
  (-> []
      (expand-with-define db vs)
      (expand-with-compose-include db vs)
      (expand-with-compose-exclude db vs)
      (apply-expansion-filters params)))

(defn expand [db vs params]
  (let [result (expand* db vs params)]
    (assoc vs :expansion {:identifier (util/uuid)
                          :timestamp (time/now)
                          :contains (map (fn [x] (dissoc x :search-vector))
                                         result)})))
