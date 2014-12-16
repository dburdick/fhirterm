(ns fhirterm.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.params :as ring-params]
            [ring.middleware.keyword-params :as ring-kw-params]
            [ring.middleware.stacktrace :as ring-stacktrace]
            [fhirterm.db :as db]
            [sqlingvo.core :as sql]
            [fhirterm.json :as json]
            [fhirterm.naming-system.core :as ns-core]
            [fhirterm.fhir.core :as fhir]
            [org.httpkit.server :as http-kit]))

(defn respond-with [status obj]
  (let [json (if (string? obj) (json/parse obj) obj)
        json-string (json/generate json {:pretty true})]
    {:status status
     :body json-string
     :content-type "application/json"}))

(defn respond-with-outcome [severity type message & [http-status]]
  (respond-with (or http-status 500)
                (fhir/make-operation-outcome severity type message)))

(defn respond-with-not-found [& [msg]]
  (respond-with-outcome :fatal :not-found
                        (or msg
                            "The requested URL could not be processed")
                        404))

(defroutes app
  (context "/ValueSet" []
    (GET "/$lookup" {params :params db :db :as request}
      (let [result (ns-core/lookup db params)]
        (if result
          (respond-with 200 (fhir/make-parameters result))
          (respond-with-not-found "Could not find requested coding"))))

    (GET "/:id" {{id :id} :params db :db}
      (let [vs (db/q-one db
                         (sql/select [*]
                           (sql/from :fhir_value_sets)
                           (sql/where `(= :id ~id))))]

        (if (or (empty? vs) (nil? vs))
          (respond-with-not-found)
          (respond-with 200 (:content vs))))))

  (route/not-found (respond-with-not-found)))

(defn assoc-into-request-mw [handler data]
  (fn [request]
    (handler (merge request data))))

(defn wrap-with-operation-outcome-exception-handler [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (respond-with-outcome :fatal :exception
                              (format "Unexpected error while processing your request:\n%s"
                                      (.getMessage e))
                              500)))))

(defn wrap-with-exception-handler [handler env]
  (if (= :production env)
    (wrap-with-operation-outcome-exception-handler handler)
    (ring-stacktrace/wrap-stacktrace handler)))

(defn- make-handler [env db]
  (-> #(app %)
      (ring-kw-params/wrap-keyword-params)
      (ring-params/wrap-params)
      (wrap-with-exception-handler env)
      (assoc-into-request-mw {:db db
                              :env env})))

(defn start [{env :env :as config} db]
  (http-kit/run-server (make-handler env db)
                       {:port (get-in config [:http :port])}))

(defn stop [server]
  (when server (server)))
