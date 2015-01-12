(ns fhirterm.core
  (:require [fhirterm.db :as db]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [fhirterm.json :as json]
            [fhirterm.system :as system]
            [fhirterm.tasks.import-valuesets :as import-vs]
            [fhirterm.tasks.import-snomed :as import-snomed]
            [fhirterm.tasks.import-loinc :as import-loinc]))

(defn- usage [options-summary]
  (->> ["FHIRTerm Terminology server"
        ""
        "Usage: fhirterm [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  import-vs      Import FHIR ValueSets into database"
        "  import-loinc   Import LOINC NS from ZIP distribution"
        "  import-snomed  Import SNOMEDCT NS from ZIP distribution"
        ""
        "Please refer to the README for more information."]
       (string/join \newline)))

(def task-to-namespace-map
  {"import-vs" import-vs/perform
   "import-loinc" import-loinc/perform
   "import-snomed" import-snomed/perform})

(def cli-options
  [["-c" "--config PATH" "Path to the configuration file"
    :default "config.json"
    :parse-fn str
    :validate [#(not (clojure.string/blank? %)) "Must be specified"]]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn- read-config [path]
  (try
    (json/parse (slurp path))

    (catch java.io.FileNotFoundException e
      (println (format "Could not read config file: %s"
                       (.getMessage e)))

      nil)

    (catch com.fasterxml.jackson.core.JsonParseException e
      (println (format "Could not parse config file %s: %s"
                       path
                       (.getMessage e)))

      nil)))

(defn- exit [status msg]
  (println msg)
  (System/exit status))

(defn- perform-run [cfg]
  (system/start cfg))

(defn- perform-task [cfg [task-name & args]]
  (let [syst (system/start cfg true)
        task-fn (get task-to-namespace-map task-name)]

    (when (not task-fn)
      (exit 1 (format "Unknown task %s.\nTry 'fhirterm help' for more information."
                      task-name)))

    (task-fn (:db system/*system*) args)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)

        config (read-config (:config options))
        action (first arguments)]

    (when (empty? config)
      (exit 1 "Incorrect or empty config file. Aborting."))

    (when errors
      (exit 1 (error-msg errors)))

    (condp = action
      "run" (perform-run config)
      "do" (perform-task config (rest arguments))
      (exit 1 (format "Unknown action: '%s'"
                      action)))))
