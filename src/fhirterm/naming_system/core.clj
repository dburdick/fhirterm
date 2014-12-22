(ns fhirterm.naming-system.core
  (:require [clojure.string :as str]))

(def uri-to-symbolic-name
  {"http://loinc.org" :loinc
   "http://snomed.info/sct" :snomed})

(defn- normalize-system-uri [uri]
  (str/replace uri #"/$" ""))

(defn- resolve-system-ns-by-uri [uri]
  (if (contains? uri-to-symbolic-name uri)
    (let [symbolic-name (get uri-to-symbolic-name uri)
          ns-name (symbol (str "fhirterm.naming-system." (name symbolic-name)))]
      (require ns-name)
      ns-name)

    nil))

(defn- invoke-ns-method [system method-name & args]
  (let [system-uri (normalize-system-uri system)
        system-ns (resolve-system-ns-by-uri system-uri)]

    (if system-ns
      (apply (ns-resolve system-ns method-name) args)
      (throw (IllegalArgumentException. (format "Unknown NamingSystem: %s" system))))))

(defn lookup-code [db {:keys [system] :as params}]
  (invoke-ns-method system 'lookup-code db params))

(defn filter-codes [db system filters]
  (invoke-ns-method system 'filter-codes db filters))
