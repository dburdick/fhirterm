(ns fhirterm.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :as http-kit]))

(defroutes app
  (GET "/" []
    "Hello world!")

  (route/not-found "<h1>Page not found</h1>"))

(defn assoc-into-request-mw [handler data]
  (fn [request]
    (handler (merge request data))))

(defn- make-handler [db]
  (-> #(app %)
      (assoc-into-request-mw {:db db})))

(defn start [config db]
  (http-kit/run-server (make-handler db)
                       {:port (get-in config [:http :port])}))

(defn stop [server]
  (when server (server)))
