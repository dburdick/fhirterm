(ns fhirterm.naming-system.snomed
  (:require [honeysql.helpers :as sql]
            [clojure.string :as str]
            [fhirterm.db :as db]
            [honeysql.core :as sqlc]))

(def snomed-uri "http://snomed.info/sct")
(def costy-threshold 10000)

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
       :display (:display found-concept)
       :designation [{:value (:display found-concept)}]})))

(defn- filter-to-subquery [{:keys [op value property] :as f}]
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

(defn- combine-subqueries [op qs]
  (let [qs (remove (fn [x] (or (nil? x) (str/blank? x))) qs)]
    (if (> 2 (count qs))
      (first qs)
      (str/join (str " " (str/upper-case (name op))  " ")
                (map (fn [q] (str "(" q ")")) qs)))))

(defn- filters-to-subquery [fs]
  (combine-subqueries :intersect
                      (map (fn [f]
                             (combine-subqueries :union
                                                 (map filter-to-subquery f)))
                           fs)))

(defn- row-to-coding [c]
  (merge c {:system snomed-uri
            :version "to.do"}))

(defn- filters-empty? [include exclude]
  (empty? (flatten [include exclude])))

(defn- filters-to-predicate [i e]
  (when (not (filters-empty? i e))
    (let [included-query (filters-to-subquery i)
          excluded-query (filters-to-subquery e)

          concept-ids-subquery
          (combine-subqueries :except [included-query excluded-query])]

      (if (and (not included-query) excluded-query)
        [:not [:in :concept_id (sqlc/raw (str "(" excluded-query ")"))]]
        [:in :concept_id (sqlc/raw (str "(" concept-ids-subquery ")"))]))))

(defn- filters-to-query [{:keys [include exclude text] :as filters}]
  (let [q (-> (sql/select [:concept_id :code] [:term :display])
              (sql/from :snomed_descriptions_no_history)
              (sql/where (filters-to-predicate include exclude)))]

    (if text
      (sql/merge-where q [:ilike :term (str "%" text "%")])
      q)))

(defn filter-codes [filters]
  (map row-to-coding
       (db/q (filters-to-query filters))))

(defn- count-codes [{:keys [include exclude] :as filters}]
  (db/q-val (let [predicate (filters-to-predicate include exclude)]
              (if (or (nil? predicate) (= (first predicate) :not))
                (-> (sql/select :%count.*)
                    (sql/from :snomed_descriptions_no_history)
                    (sql/where predicate))

                (-> (sql/select :%count.*)
                    (sql/from [(nth predicate 2) :_]))))))

(defn costy? [{:keys [include exclude] :as filters}]
  (and (not (:text filters))
       (or (filters-empty? include exclude)
           (< (count-codes filters) costy-threshold))))
