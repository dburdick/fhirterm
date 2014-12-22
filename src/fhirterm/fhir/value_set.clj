(ns fhirterm.fhir.value-set
  (:require [fhirterm.db :as db]
            [honeysql.helpers :as sql]
            [fhirterm.json :as json]
            [fhirterm.util :as util]
            [fhirterm.naming-system.core :as naming-system]
            [clj-time.core :as time]
            [clojure.set :as set]
            [clojure.string :as str]))

(defn find-by-id [db id]
  (let [result (db/q-one db (-> (sql/select :content)
                                (sql/from :fhir_value_sets)
                                (sql/where [:= :id id])))]

    (if result (json/parse (:content result)) nil)))

(defn find-by-identifier [db identifier]
  (let [result (db/q-one db (-> (sql/select :content)
                                (sql/from :fhir_value_sets)
                                (sql/where [:= :identifier identifier])))]

    (if result (json/parse (:content result)) nil)))


(defn- filters-from-include-or-exclude [includes]
  (map (fn [inc]
         (let [regular-filters (or (:filter inc) [])
               codes (or (map :code (:concept inc)) [])
               code-filter (if (empty? codes)
                             []
                             [{:op "in" :property "code" :value codes}])]

           (into regular-filters code-filter)))
       includes))

(defn- expand-with-compose-include-and-exclude [expansion db vs]
  (let [includes-by-syst (group-by :system (get-in vs [:compose :include]))
        excludes-by-syst (group-by :system (get-in vs [:compose :exclude]))
        filters-for-external-ns
        (reduce (fn [acc syst]
                  (assoc acc syst
                         {:include
                          (filters-from-include-or-exclude (get includes-by-syst syst))
                          :exclude
                          (filters-from-include-or-exclude (get excludes-by-syst syst))}))
                {} (keys includes-by-syst))]

    (reduce (fn [res [ns filters]]
              (into res (naming-system/filter-codes db ns filters)))
            expansion filters-for-external-ns)))

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

(declare expand*)
(defn- expand-with-compose-import [expansion db vs]
  (let [imports (get-in vs [:compose :import])]
    (reduce (fn [result identifier]
              (let [imported-vs (find-by-identifier db identifier)]
                (if imported-vs
                  (into result (expand* db imported-vs {}))
                  result)))
            expansion imports)))

(defn- expand* [db vs params]
  (-> []
      (expand-with-define db vs)
      (expand-with-compose-import db vs)
      (expand-with-compose-include-and-exclude db vs)
      (apply-expansion-filters params)))

(defn expand [db vs params]
  (let [result (expand* db vs params)]
    (assoc vs :expansion {:identifier (util/uuid)
                          :timestamp (time/now)
                          :contains (map (fn [x] (dissoc x :search-vector))
                                         result)})))
