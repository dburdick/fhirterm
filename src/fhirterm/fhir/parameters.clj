(ns fhirterm.fhir.parameters)

(defn- make-parameter-and-value [[key value]]
  (let [value-key (cond
                   (string? value) :valueString
                   (number? value) :valueNumeric
                   (or (true? value) (false? value)) :valueBoolean
                   (map? value) :part
                   :else :valueUnknown)
        value-value (if (map? value)
                      (map make-parameter-and-value value)
                      value)]

    {:name (name key)
     value-key value-value}))

(defn- parameters-map-to-vector [m]
  (reduce (fn [res [key value]]
            (if (coll? value)
              (into res (map (fn [v] [key v]) value))
              (conj res [key value])))
          [] m))

(defn make [key-value-map]
  {:resourceType "Parameters"
   :parameter (map make-parameter-and-value
                   (parameters-map-to-vector key-value-map))})
