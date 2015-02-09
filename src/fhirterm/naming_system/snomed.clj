(ns fhirterm.naming-system.snomed
  (:require [honeysql.helpers :as sql]
            [clojure.string :as str]
            [fhirterm.db :as db]
            [honeysql.core :as sqlc]))

(def snomed-uri "http://snomed.info/sct")

(defn lookup-code [params]
  (let [found-concept (db/q-one (-> (sql/select [:sd.term :display]
                                      [:sd.concept_id :code])
                                    (sql/from [:snomed_descriptions_no_history :sd])
                                    (sql/where [:= :sd.concept_id
                                                (java.lang.Long. (:code params))])
                                    (sql/limit 1)))]
    (when found-concept
      {:name "SNOMED"
       :version "to.do"
       :abstract "TODO"
       :display (:display found-concept)
       :designation [{:value (:display found-concept)}]})))

(defn- filter-to-query [{:keys [op value property] :as f}]
  (cond
   (and (= op "is-a") (= property "concept"))
   (-> (sql/select [:%unnest.descendants :concept_id])
       (sql/from [:snomed_ancestors_descendants :sad])
       (sql/where [:= :sad.concept_id (java.lang.Long. value)])
       (sqlc/format)
       (first))

   (and (= op "in") (= property "code"))
   (str "SELECT unnest('{"
        (str/join "," (keys value))
        "}'::bigint[]) AS concept_id")

   :else
   (throw (IllegalArgumentException. (str "Don't know how to apply filter "
                                          (pr-str f))))))

(defn- combine-queries [op qs]
  (let [qs (remove (fn [x] (or (nil? x) (str/blank? x))) qs)]
    (if (> 2 (count qs))
      (first qs)
      (str/join (str " " (str/upper-case (name op))  " ")
                (map (fn [q] (str "(" q ")")) qs)))))

(defn- filters-to-query [fs]
  (combine-queries :intersect
                   (map (fn [f]
                          (combine-queries :union
                                           (map filter-to-query f)))
                        fs)))

(defn- row-to-coding [c]
  (merge c {:system snomed-uri
            :abstract false
            :version "to.do"}))

(defn filters-empty? [i e]
  (empty? (flatten [i e])))

(defn filter-codes [{:keys [include exclude :as filters]}]
  (if (filters-empty? include exclude)
    (map row-to-coding
         (db/q (-> (sql/select [:%distinct.sc.id :code] [:sd.term :display])
                   (sql/from [:snomed_concepts :sc])
                   (sql/join [:snomed_descriptions_no_history :sd]
                             [:= :sd.concept_id :sc.id])

                   (sql/where [:= :sc.active true]))))

    (let [included-query (filters-to-query include)
          excluded-query (filters-to-query exclude)
          concept-ids-query (combine-queries :except [included-query excluded-query])]

      (map row-to-coding
           (db/q (-> (sql/select [:t.concept_id :code]
                       [:sd.term :display])
                     (sql/from [(sqlc/raw (str "(" concept-ids-query ")")) :t])
                     (sql/join [:snomed_descriptions_no_history :sd]
                               [:= :sd.concept_id :t.concept_id])))))))

(defn costy? [filters]
  (empty? (flatten (:include filters))))
