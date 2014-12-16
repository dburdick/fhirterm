(ns user
  (:require
   [clojure.pprint :refer [pprint]]
   [clojure.string :as str]
   [fhirterm.system :as system]
   [clojure.tools.namespace.repl :refer (refresh)]))

(def config
  {:env :development
   :http {:port 7654}
   :db   {:classname "org.sqlite.JDBC"
          :subprotocol "sqlite"
          :subname "db/fhirterm.sqlite3"}})

(defn start []
  (system/start config))

(defn stop []
  (system/stop))

(defn reset []
  (stop)
  (refresh :after 'user/start))
