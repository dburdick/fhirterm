(ns fhirterm.util)

(defn uuid []
  (str (java.util.UUID/randomUUID)))
