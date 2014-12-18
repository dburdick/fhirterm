(ns fhirterm.system
  (:require [fhirterm.server :as server]
            [fhirterm.db :as db]))

(def ^:dynamic *system* nil)

(defn- make-system [{env :env :as config}]
  (when (not (contains? #{:development :production} env))
    (throw (IllegalArgumentException. (format "Invalid app environment: %s"
                                              env))))
  (let [db (db/start config)]
    {:server (server/start config db)
     :db db
     :env env}))

(defn production? []
  (= (:env *system*) :production))

(defn development? []
  (= (:env *system*) :development))

(defn start [config]
  (alter-var-root #'*system*
                  (fn [system]
                    (if (not system)
                      (make-system config)
                      system))))

(defn stop []
  (alter-var-root #'*system*
                  (fn [system]
                    (server/stop (:server system)))))
