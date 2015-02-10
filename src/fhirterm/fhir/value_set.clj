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

(defn- get-composing-filters [vs params]
  (let [includes-by-syst (group-by :system (get-in vs [:compose :include]))
        excludes-by-syst (group-by :system (get-in vs [:compose :exclude]))
        grouped-filters (reduce (fn [acc syst]
                                  (assoc acc syst
                                         {:include
                                          (filters-from-include-or-exclude (get includes-by-syst syst))
                                          :exclude
                                          (filters-from-include-or-exclude (get excludes-by-syst syst))}))
                                {} (keys includes-by-syst))]

    ;; add text filter, if any
    (reduce (fn [acc [ns fs]]
              (assoc-in acc [ns :text] (:filter params)))
            grouped-filters grouped-filters)))

(defn- expand-with-compose-include-and-exclude [expansion vs params]
  (let [filters-by-ns (get-composing-filters vs params)]
    (reduce (fn [res [ns filters]]
              (let [system (resolve-naming-system ns)]
                (into res (naming-system/filter-codes system filters))))
            expansion filters-by-ns)))

(defn- expand-with-define [expansion {{:keys [system concept]} :define :as vs} params]
  (let [result (reduce (fn reduce-fn [result c]
                         (let [result (conj result
                                            {:code    (:code c)
                                             :display (:display c)
                                             :system  system})
                               inner-concept (:concept c)]
                           (if inner-concept
                             (into result (reduce reduce-fn [] inner-concept))
                             result)))
                       expansion
                       concept)]

    ;; apply text filter, if present
    (if (:filter params)
      (filter #(.equalsIgnoreCase % (:filter params)) result))))

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
(defn- expand-with-compose-import [expansion vs params]
  (let [imports (get-in vs [:compose :import])]
    (reduce (fn [result identifier]
              (let [imported-vs (find-by-identifier identifier)]
                (if imported-vs
                  (into result (expand* imported-vs params))
                  result)))
            expansion imports)))

(declare costy-expansion?)
(defn- costy-import? [vs params]
  (let [imports (get-in vs [:compose :import])]
    (reduce (fn [result identifier]
              (if (not result)
                (let [imported-vs (find-by-identifier identifier)]
                  (if imported-vs
                    (costy-expansion? imported-vs params)
                    result))

                result))
            false imports)))

(defn- costy-compose? [vs params]
  (let [filters-by-ns (get-composing-filters vs params)]
    (println "!!!!" (pr-str filters-by-ns))
    (reduce (fn [res [ns filters]]
              (if (not res)
                (let [system (resolve-naming-system ns)]
                  (naming-system/costy? system filters))
                res))
            false filters-by-ns)))

;; TODO: rewrite with algo.monads
(defn- expand* [vs params]
  (-> []
      (expand-with-define vs params)
      (expand-with-compose-import vs params)
      (expand-with-compose-include-and-exclude vs params)))

;; TODO: rewrite with algo.monads
(defn- costy-expansion? [vs params]
  (or (costy-import? vs params)
      (costy-compose? vs params)))

(defn expand [vs params]
  (if (costy-expansion? vs params)
    :too-costy

    (let [result (expand* vs params)]
      (assoc vs :expansion {:identifier (util/uuid)
                            :timestamp (time/now)
                            :contains (map (fn [x] (dissoc x :search-vector))
                                           result)}))))
