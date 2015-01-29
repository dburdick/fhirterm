(ns fhirterm.fhir.value-set
  (:require [fhirterm.json :as json]
            [fhirterm.util :as util]
            [fhirterm.fhir.client :as fhir-client]
            [fhirterm.naming-system.core :as naming-system]
            [clj-time.core :as time]
            [clojure.string :as str]))

(defn find-by-id [id]
  (fhir-client/get-resource "ValueSet" id))

(defn find-by-identifier [identifier]
  (get-in (fhir-client/search "ValueSet" {:identifier identifier})
          [:entry 0 :resource]))

(defn find-vs-defining-ns [ns-uri]
  (get-in (fhir-client/search "ValueSet" {:system ns-uri})
          [:entry 0 :resource]))

(defn- resolve-naming-system [ns-uri]
  (if (naming-system/known? ns-uri)
    ns-uri

    (let [vs-ns (find-vs-defining-ns ns-uri)]
      (if vs-ns
        vs-ns
        (throw (IllegalArgumentException. (format "Unknown NamingSystem: %s"
                                                  ns-uri)))))))

(defn- filters-from-include-or-exclude [includes]
  (map (fn [inc]
         (let [regular-filters (or (:filter inc) [])
               concepts (reduce (fn [acc c]
                                  (assoc acc (:code c) c))
                                {} (:concept inc))

               code-filter (if (empty? concepts)
                             []
                             [{:op "in" :property "code" :value concepts}])]

           (into regular-filters code-filter)))
       includes))

(defn- expand-with-compose-include-and-exclude [expansion vs]
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
              (let [system (resolve-naming-system ns)]
                (into res (naming-system/filter-codes system filters))))
            expansion filters-for-external-ns)))

(defn- expand-with-define [expansion {{:keys [system concept]} :define :as vs}]
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
(defn- expand-with-compose-import [expansion vs]
  (let [imports (get-in vs [:compose :import])]
    (reduce (fn [result identifier]
              (let [imported-vs (find-by-identifier identifier)]
                (if imported-vs
                  (into result (expand* imported-vs {}))
                  result)))
            expansion imports)))

(defn- expand* [vs params]
  (-> []
      (expand-with-define vs)
      (expand-with-compose-import vs)
      (expand-with-compose-include-and-exclude vs)
      (apply-expansion-filters params)))

(defn expand [vs params]
  (let [result (expand* vs params)]
    (assoc vs :expansion {:identifier (util/uuid)
                          :timestamp (time/now)
                          :contains (map (fn [x] (dissoc x :search-vector))
                                         result)})))
