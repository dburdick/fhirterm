(ns fhirterm.system
  (:require [fhirterm.server :as server]
            [fhirterm.db :as db]))

(def ^:dynamic *system* nil)

(defn- make-system [{env :env :as config} headless?]
  (when (not (contains? #{:development :production} (keyword env)))
    (throw (IllegalArgumentException. (format "Invalid app environment: %s"
                                              env))))
  (let [db (db/start config)]
    {:server (if headless? nil (server/start config db))
     :db db
     :env (keyword env)}))

(defn production? []
  (= (:env *system*) :production))

(defn development? []
  (= (:env *system*) :development))

(defn start [config & [headless?]]
  (alter-var-root #'*system*
                  (fn [system]
                    (if (not system)
                      (make-system config headless?)
                      system))))

(defn stop []
  (alter-var-root #'*system*
                  (fn [{server :server :as system}]
                    (when server
                      (server/stop server)))))
