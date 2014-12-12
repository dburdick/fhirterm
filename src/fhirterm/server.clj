(ns fhirterm.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [fhirterm.db :as db]
            [sqlingvo.core :as sql]
            [fhirterm.json :as json]
            [org.httpkit.server :as http-kit]))

(defn respond-with-json [status obj]
  (let [json (if (string? obj) obj (json/generate obj {:pretty true}))]
    {:status status
     :body json
     :content-type "application/json"}))

(defn respond-with-not-found []
  (respond-with-json 404 {:resourceType "OperationOutcome"
                          :text "Not found!"
                          :todo "implement this response"}))

(defroutes app
  (context "/ValueSet" []
    (GET "/:id" {{id :id} :params db :db}
      (let [vs (db/q db
                     (sql/select [*]
                       (sql/from :fhir_value_sets)
                       (sql/where `(= :id ~id))))]

        (if (empty? vs)
          (respond-with-not-found)
          (respond-with-json 200 (:content (first vs)))))))

  (route/not-found (respond-with-not-found)))

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
