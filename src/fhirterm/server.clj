(ns fhirterm.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.params :as ring-params]
            [ring.middleware.keyword-params :as ring-kw-params]
            [fhirterm.db :as db]
            [sqlingvo.core :as sql]
            [fhirterm.json :as json]
            [fhirterm.naming-system.core :as ns-core]
            [fhirterm.fhir.parameters :as fhir-parameters]
            [fhirterm.fhir.operation-outcome :as fhir-op-outcome]
            [org.httpkit.server :as http-kit]))

(defn respond-with [status obj]
  (let [json (if (string? obj) obj (json/generate obj {:pretty true}))]
    {:status status
     :body json
     :content-type "application/json"}))

(defn respond-with-outcome [severity type message & [http-status]]
  (respond-with (or http-status 500)
                (fhir-op-outcome/make severity type message)))

(defn respond-with-not-found []
  (respond-with-outcome :fatal :not-found
                        "The requested URL could not be processed"
                        404))

(defroutes app
  (context "/ValueSet" []
    (GET "/$lookup" {params :params db :db :as request}
      (respond-with 200 (fhir-parameters/make (ns-core/lookup db params))))

    (GET "/:id" {{id :id} :params db :db}
      (let [vs (db/q-one db
                         (sql/select [*]
                           (sql/from :fhir_value_sets)
                           (sql/where `(= :id ~id))))]

        (if (empty? vs)
          (respond-with-not-found)
          (respond-with 200 (:content vs))))))

  (route/not-found (respond-with-not-found)))

(defn assoc-into-request-mw [handler data]
  (fn [request]
    (handler (merge request data))))

(defn- make-handler [db]
  (-> #(app %)
      (ring-kw-params/wrap-keyword-params)
      (ring-params/wrap-params)
      (assoc-into-request-mw {:db db})))

(defn start [config db]
  (http-kit/run-server (make-handler db)
                       {:port (get-in config [:http :port])}))

(defn stop [server]
  (when server (server)))
