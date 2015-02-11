(ns fhirterm.naming-system.rxnorm
  (:require [honeysql.helpers :as sql]
            [clojure.string :as str]
            [fhirterm.db :as db]
            [honeysql.core :as sqlc]))

(def rxnorm-uri "http://www.nlm.nih.gov/research/umls/rxnorm")

(def rxn-relationship-types
  ["active_ingredient_of" "active_metabolites_of" "chemical_structure_of"
   "consists_of" "constitutes" "contained_in"
   "contains" "contraindicated_with_disease" "contraindicating_class_of"
   "contraindicating_mechanism_of_action_of"
   "contraindicating_physiologic_effect_of" "doseformgroup_of"
   "dose_form_of" "effect_may_be_inhibited_by"
   "entry_version_of" "form_of" "has_active_ingredient"
   "has_active_metabolites" "has_chemical_structure" "has_contraindicated_drug"
   "has_contraindicating_class" "has_contraindicating_mechanism_of_action"
   "has_contraindicating_physiologic_effect" "has_doseformgroup" "has_dose_form"
   "has_entry_version" "has_form" "has_ingredient"
   "has_ingredients" "has_mechanism_of_action" "has_member"
   "has_part" "has_participant" "has_permuted_term"
   "has_pharmacokinetics" "has_physiologic_effect" "has_precise_ingredient"
   "has_print_name" "has_product_component" "has_quantified_form"
   "has_sort_version" "has_therapeutic_class" "has_tradename"
   "included_in" "includes" "induced_by"
   "induces" "ingredients_of" "ingredient_of"
   "inverse_isa" "isa" "mapped_from"
   "mapped_to" "may_be_diagnosed_by" "may_be_prevented_by"
   "may_be_treated_by" "may_diagnose" "may_inhibit_effect_of"
   "may_prevent" "may_treat" "mechanism_of_action_of"
   "member_of" "metabolic_site_of" "participates_in"
   "part_of" "permuted_term_of" "pharmacokinetics_of"
   "physiologic_effect_of" "precise_ingredient_of" "print_name_of"
   "product_component_of" "quantified_form_of" "reformulated_to"
   "reformulation_of" "site_of_metabolism" "sort_version_of"
   "therapeutic_class_of" "tradename_of"])

(defn- row-to-coding [c]
  (merge c {:system rxnorm-uri
            :abstract false
            :version "to.do"}))

(defn filters-empty? [i e]
  (empty? (flatten [i e])))

(defn- split-column-and-value [v]
  (if (not= (.indexOf v ":") -1)
    (str/split v #":" 2)
    [nil v]))

(def rxn-relationships
  #{"SY" "SIB" "RN" "PAR" "CHD" "RB" "RO"})

(defn- filter-to-subquery [{:keys [op value property] :as f}]
  (cond
   (and (= op "in") (= property "code"))
   (str "SELECT unnest('{"
        (str/join "," (keys value))
        "}'::varchar[]) AS rxcui")

   ;; 1.15.1.2.4.1
   (and (= op "=") (= property "STY"))
   (let [[clmn val] (split-column-and-value value)]
     (format "SELECT rxcui FROM rxn_sty
              WHERE %s = %s"
             (or clmn "tui") (db/quote-str val)))

   ;; 1.15.1.2.4.2
   (and (= op "=") (= property "SAB"))
   (format "SELECT rxcui FROM rxn_conso WHERE sab = %s"
           (db/quote-str value))

   ;; 1.15.1.2.4.3
   (and (= op "=") (= property "TTY"))
   (format "SELECT rxcui FROM rxn_conso WHERE tty = %s"
           (db/quote-str value))

   ;; 1.15.1.2.4.4
   ;; 1.15.1.2.4.5
   (or
    (and (= op "=") (contains? rxn-relationships property))
    (and (= op "=") (contains? rxn-relationship-types property)))

   (let [clmn (if (contains? rxn-relationships property) "rel" "rela")
         [attr val] (str/split value #":" 2)]
     (when (or (not attr) (not val))
       (throw (IllegalArgumentException. (str "Incorrect filter: " (pr-str f)))))

     (if (= attr "CUI")
       (format "SELECT rxcui1 AS rxcui FROM rxn_rel WHERE %s = %s
                AND rxcui2 = %s"
               clmn (db/quote-str property) (db/quote-str val))

       (format "SELECT conso.rxcui FROM rxn_rel
                JOIN rxn_conso conso ON conso.rxaui = rxn_rel.rxaui1
                WHERE %s = %s AND rxaui2 = %s"
               clmn (db/quote-str property) (db/quote-str val))))

   :else
   (throw (IllegalArgumentException. (str "Don't know how to apply filter: "
                                          (pr-str f))))))

(defn- combine-queries [op qs]
  (let [qs (remove (fn [x] (or (nil? x) (str/blank? x))) qs)]
    (if (> 2 (count qs))
      (first qs)
      (str/join (str " " (str/upper-case (name op))  " ")
                (map (fn [q] (str "(" q ")")) qs)))))

(defn- filters-to-subquery [fs]
  (combine-queries :intersect
                   (map (fn [f]
                          (combine-queries :union
                                           (map filter-to-subquery f)))
                        fs)))

(defn filters-to-query [{:keys [include exclude text] :as filters}]
  (let [q (if (filters-empty? include exclude)
            (-> (sql/select [:rxcui :code] [:%max.str :display])
                (sql/from :rxn_conso)
                (sql/where [:and
                            [:= :sab "RXNORM"]
                            [:<> :tty "SY"]])
                (sql/group :rxcui))

            (let [included-subquery (filters-to-subquery include)
                  excluded-subquery (filters-to-subquery exclude)

                  concept-ids-subquery
                  (sqlc/raw
                   (str "("
                        (combine-queries :except [included-subquery
                                                  excluded-subquery])
                        ")"))]

              (-> (sql/select [:rxcui :code] [:%max.str :display])
                  (sql/from :rxn_conso)
                  (sql/where [:and
                              [:= :sab "RXNORM"] [:<> :tty "SY"]
                              (if (and (not included-subquery) excluded-subquery)
                                [:not [:in :rxcui concept-ids-subquery]]
                                [:in :rxcui concept-ids-subquery])])
                  (sql/group :rxcui))))]
    (if text
      (sql/merge-where q [:ilike :str (str "%" text "%")])
      q)))

(defn filter-codes [filters]
  (let [q (filters-to-query filters)]
    (map row-to-coding (db/q q))))

(defn costy? [filters]
  (and (not (:text filters))
       (filters-empty? (:include filters) [])))
