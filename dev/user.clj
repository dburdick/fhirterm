(ns user
  (:require
   [clojure.pprint :refer [pprint]]
   [clojure.string :as str]
   [fhirterm.system :as system]))

(def config
  {:http {:port 7654}})

(defn start []
  (system/start config))

(defn stop []
  (system/stop))

(defn restart []
  (stop)
  (start))
