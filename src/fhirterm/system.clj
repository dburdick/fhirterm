(ns fhirterm.system
  (:require [fhirterm.server :as server]
            [fhirterm.json :as json]
            [fhirterm.db :as db]
            [clojure.string :as str]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def ^:dynamic *system* nil)

(defn- make-system [{env :env log :log :as config} headless?]
  (when (empty? config)
    (throw (IllegalArgumentException. "nil or empty config passed to system/start")))

  (when (not (contains? #{:development :production} (keyword env)))
    (throw (IllegalArgumentException. (format "Invalid app environment: %s"
                                              env))))

  (timbre/set-config! [:appenders :spit :enabled?] true)
  (timbre/set-config! [:shared-appender-config :spit-filename] (:file log))
  (timbre/set-level! (keyword (str/lower-case (or (System/getenv "LOG_LEVEL")
                                                  (:level log)
                                                  "info"))))

  (debug "Log initialized")

  (let [db (db/start config)]
    {:server (if headless? nil (server/start config db))
     :db db
     :env (keyword env)}))

(defn production? []
  (= (:env *system*) :production))

(defn development? []
  (= (:env *system*) :development))

(defn read-config [path]
  (try
    (json/parse (slurp path))

    (catch java.io.FileNotFoundException e
      (println (format "Could not read config file: %s"
                       (.getMessage e)))

      nil)

    (catch com.fasterxml.jackson.core.JsonParseException e
      (println (format "Could not parse config file %s: %s"
                       path
                       (.getMessage e)))

      nil)))

(defn stop []
  (alter-var-root #'*system*
                  (fn [{server :server :as system}]
                    (when server
                      (server/stop server))

                    nil)))

(defn start [config & [headless?]]
  (alter-var-root #'*system*
                  (fn [system]
                    (stop)
                    (if (not system)
                      (make-system config headless?)
                      system))))

(defn start-headless [config]
  (start config true))
