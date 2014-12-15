(ns fhirterm.naming-system.core)

(def uri-to-symbolic-name
  {"http://loinc.org" :loinc})

(defn- resolve-system-ns-by-uri [uri]
  (let [symbolic-name (get uri-to-symbolic-name uri)
        ns-name (symbol (str "fhirterm.naming-system." (name symbolic-name)))]
    (require ns-name)
    ns-name))

(defn lookup [db {:keys [system code] :as params}]
  (let [system-ns (resolve-system-ns-by-uri system)
        lookup-fn (ns-resolve system-ns 'lookup)]
    (lookup-fn db params)))
