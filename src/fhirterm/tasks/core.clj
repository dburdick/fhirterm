(ns fhirterm.tasks.core
  (:require [fhirterm.db :as db]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [fhirterm.tasks.import-valuesets :as ivs]
            [fhirterm.tasks.import-loinc :as import-loinc]))

(defn- usage [options-summary]
  (->> ["FHIRTerm command-line utility"
        ""
        "Usage: lein task [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  import-vs     Import FHIR ValueSets into database"
        ""
        "Please refer to the README for more information."]
       (string/join \newline)))

(def cli-options
  [["-d" "--db PATH" "Path to SQLite database file"
    :default ""
    :parse-fn str
    :validate [#(not (clojure.string/blank? %)) "Must be specified"]]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn- options-to-config [options]
  {:db {:classname "org.sqlite.JDBC"
        :subprotocol "sqlite"
        :subname (:db options)}})

(def task-to-namespace-map
  {"import-vs" ivs/perform
   "import-loinc" import-loinc/perform})

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        task-fn (get task-to-namespace-map (first arguments))]

    (cond
     (not task-fn) (exit 1 (usage summary))
     errors (exit 1 (error-msg errors)))

    (let [db (db/start (options-to-config options))]
      (task-fn db (rest arguments)))))
