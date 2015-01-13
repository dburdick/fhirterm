(ns fhirterm.integration-test
  (:require [fhirterm.system :as system]
            [clojure.test :refer :all]
            [fhirterm.json :as json]
            [clojure.string :as str]
            [org.httpkit.client :as http]))

(def ^:dynamic *config* nil)

(defn start-server-fixture [f]
  (println "Starting test server")

  (alter-var-root #'*config*
                  (constantly (system/read-config "test/config.json")))

  (let [system (system/start *config*)]
    (f)
    (println "Stopping server")
    (system/stop)))

(use-fixtures :once start-server-fixture)

(defn make-url [& p]
  (let [base (format "http://localhost:%d" (get-in *config* [:http :port]))]
    (str/join "/" (into [base] p))))

(defn expand-vs [id]
  (let [response @(http/get (make-url "ValueSet" id "$expand"))]

    (when (nil? (:body response))
      (println "!!!" (pr-str response)))

    (json/parse (slurp (:body response)))))

(defn get-expansion [r]
  (get-in r [:expansion :contains]))

(defn find-coding [codings code]
  (first (filter (fn [c] (= (:code c) code)) codings)))

(deftest ^:integration expansion-of-vs-with-enumerated-loinc-codes-test
  (let [result (get-expansion (expand-vs "lipid-ldl-codes"))]

    (is (find-coding result "13457-7")
        "enumerated code is present in expansion result")

    (is (find-coding result "18262-6")
        "enumerated code is present in expansion result")

    (is (= (count result) 2)
        "two codings in expansion")))

(deftest ^:integration expansion-of-vs-with-entire-loinc-included-test
  (let [result (get-expansion (expand-vs "valueset-observation-codes"))]
    (is (= (count result) 73889))))

(deftest ^:integration expansion-of-vs-with-loinc-filtered-by-order-obs-test
  (let [result (get-expansion (expand-vs "valueset-diagnostic-requests"))]

    (is (find-coding result "1007-4"))
    (is (find-coding result "44241-8"))

    (is (= (count result) 38375))))
