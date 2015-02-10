(ns fhirterm.util)
(import 'fhirterm.StringUtil)

(defn uuid []
  (str (java.util.UUID/randomUUID)))

(defn string-contains? [^String s ^String what & [ignore-case]]
  (if ignore-case
    (StringUtil/containsIgnoreCase s what)
    (.contains s what)))
