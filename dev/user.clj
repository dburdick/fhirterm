(ns user
  (:require
   [clojure.pprint :refer [pprint]]
   [clojure.string :as str]
   [clojure.tools.namespace.repl :refer (refresh)]))

(def config
  {:env :development
   :http {:port 7654}
   :db   {:classname "org.postgresql.Driver"
          :subprotocol "postgresql"
          :user "fhirterm"
          :password "fhirterm"
          :subname "//127.0.0.1:5432/fhirterm"}
   :log {:file "log/fhirterm.log"
         :level "debug"}

   :fhir-client {:protocol "rest"
                 :base-url "http://localhost:3000"}})

(defn start []
  (require 'fhirterm.system)
  ((ns-resolve 'fhirterm.system 'start) config))

(defn stop []
  (require 'fhirterm.system)
  ((ns-resolve 'fhirterm.system 'start)))

(defn reset []
  (stop)
  (refresh :after 'user/start))
