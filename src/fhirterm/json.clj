(ns fhirterm.json
  (:require [cheshire.core :as json]))

(defn parse [str]
  (json/parse-string str keyword))

(defn generate [data]
  (json/generate-string data))
