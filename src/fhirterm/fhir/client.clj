(ns fhirterm.fhir.client
  (:require [fhirterm.fhir.client.rest :as rest]))

(def ^:dynamic *client* nil)

(defn- make-client [{protocol :protocol base-url :base_url :as config}]
  (when (not= protocol "rest")
    (throw (IllegalArgumentException. (str "Unknown FHIR client protocol: " protocol))))

  (let [ns (condp = protocol
             "rest" 'fhirterm.fhir.client.rest)]
    (merge {:ns ns :protocol protocol}
           ((ns-resolve ns 'start) config))))

(defn- invoke [fn-name & args]
  (when (not *client*)
    (throw (RuntimeException. "No FHIR Client instance started")))

  (let [f (ns-resolve (:ns *client*) (symbol fn-name))]
    (when (not f)
      (throw (IllegalArgumentException. (format "No function named %s found in ns %s"
                                                fn-name (:ns *client*)))))

    (apply f *client* args)))

(defn start [{config :fhir-client}]
  (alter-var-root #'*client*
                  (fn [x] (make-client config)))

  *client*)

(defn stop []
  (alter-var-root #'*client*
                  (fn [x]
                    (when *client*
                      (invoke 'stop))
                    nil))
  nil)

(defn create-resource [type content]
  (invoke 'create-resource type content))

(defn resource-exists? [type id]
  (invoke 'resource-exists? type id))

(defn get-description []
  (invoke 'get-description))

(defn get-resource [type id]
  (invoke 'get-resource type id))

(defn search [type params]
  (invoke 'search type params))
