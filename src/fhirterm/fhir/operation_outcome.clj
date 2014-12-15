(ns fhirterm.fhir.operation-outcome)

(def severities #{:fatal :warning :error :information})

(defn make [severity type message & [locations]]
  (when (not (contains? severities severity))
    (throw (IllegalArgumentException. (format "Severity %s is not valid" severity))))

  {:resourceType "OperationOutcome"
   :issue [{:severity severity
            :type {:system "http://hl7.org/fhir/issue-type"
                   :code type}
            :location locations
            :details message}]})
