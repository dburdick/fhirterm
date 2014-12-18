(ns fhirterm.json
  (:require [cheshire.core :as json]
            [cheshire.generate :as json-generate]
            [clj-time.format :as tfmt]
            [clj-time.core :as t]))

(def date-to-json-formatter (tfmt/with-zone
                              (tfmt/formatters :date-time)
                              t/utc))

(json-generate/add-encoder org.joda.time.DateTime
                           (fn [d json-generator]
                             (.writeString json-generator
                                           (tfmt/unparse date-to-json-formatter d))))

(json-generate/add-encoder java.io.File
                           (fn [f json-generator]
                             (.writeString json-generator
                                           (pr-str f))))


(defn parse [str]
  (json/parse-string str keyword))

(defn generate [data & options]
  (apply json/generate-string data options))
