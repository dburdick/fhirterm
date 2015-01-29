(ns fhirterm.naming-system.core
  (:require [clojure.string :as str]
            [fhirterm.naming-system.vs-defined :as vs-defined-ns]))

(def uri-to-symbolic-name
  {"http://loinc.org" :loinc
   "http://snomed.info/sct" :snomed
   "http://unitsofmeasure.org" :ucum})

(defn- normalize-system-uri [uri]
  (str/replace uri #"/$" ""))

(defn- resolve-system-ns-by-uri [uri]
  (if (contains? uri-to-symbolic-name uri)
    (let [symbolic-name (get uri-to-symbolic-name uri)
          ns-name (symbol (str "fhirterm.naming-system." (name symbolic-name)))]
      (require ns-name)
      ns-name)

    nil))

(defn known? [system-uri]
  (contains? uri-to-symbolic-name (normalize-system-uri system-uri)))

(defn- invoke-ns-method [system method-name & args]
  (if (string? system)
    (let [system-uri (normalize-system-uri system)
          system-ns (resolve-system-ns-by-uri system-uri)]

      (if system-ns
        (apply (ns-resolve system-ns method-name) args)
        (throw (IllegalArgumentException. (format "Unknown NamingSystem: %s"
                                                  system-uri)))))

    (apply (ns-resolve 'fhirterm.naming-system.vs-defined method-name)
           system args)))

(defn lookup-code [{:keys [system] :as params}]
  (invoke-ns-method system 'lookup-code params))

(defn filter-codes [system filters]
  (invoke-ns-method system 'filter-codes filters))
