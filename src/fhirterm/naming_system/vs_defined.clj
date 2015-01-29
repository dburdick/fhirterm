(ns fhirterm.naming-system.vs-defined)

(defn- concept-matches-filter? [{:keys [property op value]} path concept]
  (when (not (contains? #{"is-a" "in"} op) )
    (throw (IllegalArgumentException.
            (format "Unknown filtering operation: %s" op))))

  (when (not (contains? #{"concept" "code"} property))
    (throw (IllegalArgumentException.
            (format "Unknown filtering property: %s" concept))))

  (let [matches (condp = op
                  "is-a" (not (empty? (filter (partial = value) path)))
                  "in"   (contains? value (:code concept)))]

    (and matches
         (not (:abstract concept)))))

(defn- concept-matches-filters? [filters path concept]
  (reduce (fn [r fs]
            (or r
                (reduce (fn [r f]
                          (and r (concept-matches-filter? f path concept)))
                        true fs)))
          false filters))

(defn- check-concept [{:keys [include exclude]} path concept]
  (and (concept-matches-filters? include path concept)
       (not (concept-matches-filters? exclude path concept))))

(defn- filter-concepts [concepts f path]
  (reduce (fn [acc c]
            (let [nested (:concept c)
                  c-without-nested (dissoc c :concept)
                  acc (if (f path c-without-nested)
                        (conj acc c-without-nested)
                        acc)]

              (if (seq nested)
                (into acc (filter-concepts nested f
                                           (conj path (:code c))))
                acc)))
          []
          concepts))

(defn filter-codes [{{concepts :concept} :define :as vs} filters]
  (filter-concepts concepts
                   (partial check-concept filters)
                   []))

(defn lookup-code [vs params])
