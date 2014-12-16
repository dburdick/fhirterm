(ns fhirterm.naming-system.core
  (:require [clojure.string :as str]))

(def uri-to-symbolic-name
  {"http://loinc.org" :loinc})

(defn- normalize-system-uri [uri]
  (str/replace uri #"/$" ""))

(defn- resolve-system-ns-by-uri [uri]
  (if (contains? uri-to-symbolic-name uri)
    (let [symbolic-name (get uri-to-symbolic-name uri)
          ns-name (symbol (str "fhirterm.naming-system." (name symbolic-name)))]
      (require ns-name)
      ns-name)

    nil))

(defn lookup [db {:keys [system code] :as params}]
  (let [system-uri (normalize-system-uri system)
        system-ns (resolve-system-ns-by-uri system-uri)]
    (when system-ns
      ((ns-resolve system-ns 'lookup) db params))))
