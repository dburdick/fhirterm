(ns fhirterm.system
  (:require [fhirterm.server :as server]))

(def system nil)

(defn- make-system [config]
  {:server (server/start config)})

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
