(ns fhirterm.naming-system.rxnorm
  (:require [honeysql.helpers :as sql]
            [clojure.string :as str]
            [fhirterm.db :as db]
            [honeysql.core :as sqlc]))

(def rxnorm-uri "http://www.nlm.nih.gov/research/umls/rxnorm")

(defn- row-to-coding [c]
  (merge c {:system rxnorm-uri
            :abstract false
            :version "to.do"}))

(defn filters-empty? [i e]
  (empty? (flatten [i e])))

(defn- all-codes []
  (map row-to-coding
       (db/q (-> (sql/select [:rxcui :code] [:str :display])
                 (sql/from :rxn_conso)
                 (sql/where [:and
                             [:= :sab "RXNORM"]
                             [:<> :tty "SY"]])))))

(defn- split-column-and-value [v]
  (if (not= (.indexOf v ":") -1)
    (str/split v #":" 2)
    [nil v]))

(defn- filter-to-query [{:keys [op value property] :as f}]
  (cond
   (and (= op "in") (= property "code"))
   (str "SELECT unnest('{"
        (str/join "," (keys value))
        "}'::varchar[]) AS rxcui")

   (and (= op "=") (= property "STY"))
   (let [[clmn val] (split-column-and-value value)]
     (format "SELECT rxcui FROM rxn_sty WHERE %s = %s"
             (or clmn "tui") (db/quote-str val)))

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

(defn filter-codes [{:keys [include exclude :as filters]}]
  (if (filters-empty? include exclude)
    (all-codes)

    (let [included-query (filters-to-query include)
          excluded-query (filters-to-query exclude)
          concept-ids-query (combine-queries :except [included-query excluded-query])]

      (map row-to-coding
           (db/q (-> (sql/select [:rxcui :code] [:str :display])
                     (sql/from :rxn_conso)
                     (sql/where [:and
                                 [:= :sab "RXNORM"]
                                 [:<> :tty "SY"]
                                 [:in
                                  :rxcui
                                  (sqlc/raw (str "(" concept-ids-query ")"))]])))))))
