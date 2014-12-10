(ns fhirterm.system
  (:require [fhirterm.server :as server]
            [fhirterm.db :as db]))

(def system nil)

(defn- make-system [config]
  (let [db (db/start config)]
    {:server (server/start config db)
     :db db}))

(defn start [config]
  (alter-var-root #'system
                  (fn [system]
                    (if (not system)
                      (make-system config)
                      system))))

(defn stop []
  (alter-var-root #'system
                  (fn [system]
                    (server/stop (:server system)))))
