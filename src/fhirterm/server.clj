(ns fhirterm.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :as http-kit]))

(defroutes app
  (GET "/" [] "<h1>Hello World</h1>")
  (route/not-found "<h1>Page not found</h1>"))

(defn start [config]
  (http-kit/run-server app {:port (get-in config [:http :port])}))

(defn stop [server]
  (when server (server)))
