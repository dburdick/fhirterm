(ns fhirterm.naming-system.loinc
  (:require [honeysql.helpers :as sql]
            [clojure.string :as str]
            [fhirterm.db :as db]))

(def loinc-uri "http://loinc.org")

(defn lookup-code [db params]
  (let [found-loinc (db/q-one db (-> (sql/select :*)
                                     (sql/from :loinc_loincs)
                                     (sql/where [:= :loinc_num (:code params)])
                                     (sql/limit 1)))]
    (when found-loinc
      {:name "LOINC"
       :version "to.do"
       :abstract false
       :display (:shortname found-loinc)
       :designation [{:value (:shortname found-loinc)}
                     {:value (:long_common_name found-loinc)}]})))

(def property-to-column-map
  {"code" :loinc_num
   "order_obs" :order_obs
   "scale_typ" :scale_type})

(def value-fixup-map
  {:scale_type {"DOC" "Doc"}})

(defn- filter-to-sql-cond [f]
  (let [property (:property f)
        column (get property-to-column-map
                    (str/lower-case property)
                    (keyword (str/lower-case property)))]

    (case (:op f)
      "=" [:= column (get-in value-fixup-map [column (:value f)]
                             (:value f))]
      "in" [:in column (:value f)]
      (throw (IllegalArgumentException. (format "Unknown filtering op: %s" (:op f)))))))

(defn- filters-to-sql-cond [filters]
  (let [predicate (if (empty? filters)
                    [:= 1 1]
                    (into [:or] (map (fn [fs]
                                       (into [:and] (map filter-to-sql-cond fs)))
                                     filters)))]
    [:and
     predicate
     [:in :status ["ACTIVE" "TRIAL" "DISCOURAGED"]]]))

(defn- row-to-coding [row]
  {:system loinc-uri
   :abstract false
   :version "to.do"
   :code (:loinc_num row)
   :display (:shortname row)
   :search-vector (str/lower-case (:shortname row))})

(defn filter-codes [db filters]
  (let [codings (db/q db (-> (sql/select :loinc_num :shortname)
                             (sql/from :loinc_loincs)
                             (sql/where (filters-to-sql-cond filters))))]
    (map row-to-coding codings)))
