(ns fhirterm.naming-system.snomed
  (:require [honeysql.helpers :as sql]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.data.int-map :as int-map]
            [fhirterm.db :as db]))

(def snomed-uri "http://snomed.info/sct")
(def concepts (atom nil))

;; "Elapsed time: 7317.066 msecs"
(defn warmup [db]
  (swap! concepts
         (fn [c]
           (let [r (db/q db (-> (sql/select [:c.id :code] [:d.term :display]
                                            [:ad.ancestors :ancestors]
                                            [:ad.descendants :descendants])
                                (sql/from [:snomed_concepts :c])
                                (sql/join [:snomed_descriptions :d]
                                          [:and [:= :d.concept_id :c.id]
                                           [:= :d.active 1]
                                           [:= :d.type_id 900000000000003001]]

                                          [:snomed_ancestors_descendants :ad]
                                          [:= :ad.concept_id :c.id])

                                (sql/where [:= :c.active 1])
                                (sql/group :c.id)))]

             (reduce (fn [acc {:keys [code display ancestors descendants]}]
                       (assoc acc code
                              {:data {:search-vector (str/lower-case display)
                                      :system snomed-uri
                                      :code code
                                      :display display
                                      :version "to.do"}
                               ;;:ancestors (map long (str/split ancestors #"\s"))
                               ;;:descendants (map long (str/split descendants #"\s"))
                               }))
                     (int-map/int-map) r))))

  nil)

;; body structure: 91723000 - 60ms / 28270
;; other stuff:  404684003 - 560ms / 131678

(defn lookup-code [db params])

(defn- filter-result [db [{:keys [property op value] :as filter}]]
  (if-not (and (= op "is-a") (= property "concept"))
    (throw (RuntimeException. (format "Don't know how to filter SNOMED with %s"
                                      (pr-str filter)))))

  ;; (if (or (nil? @is-a-relations) (nil? @concepts))
  ;;   (warmup db))
  )

;; todo: support excludes
(defn filter-codes [db {:keys [include exclude :as filters]}]
  (let [included-set (time (reduce int-map/union
                                   (map (partial filter-result db) include)))
        included-codings (map (fn [concept] (get @concepts concept))
                              included-set)]

    (println "!!! Expanded codings count:" (count included-codings))
    included-codings))
