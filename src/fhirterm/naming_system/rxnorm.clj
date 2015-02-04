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

(defn filter-codes [{:keys [include exclude :as filters]}]
  (if (filters-empty? include exclude)
    (all-codes)

    []))
