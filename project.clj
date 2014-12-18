(defproject fhirterm "0.1.0-SNAPSHOT"
  :description "FHIR Terminology Server"
  :url "http://github.com/fhirbase/fhirterm"

  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.xerial/sqlite-jdbc "3.8.7"]
                 [compojure "1.3.1"]
                 [http-kit "2.1.16"]
                 [org.clojure/tools.cli "0.3.1"]
                 [honeysql "0.4.3"]
                 [cheshire "5.4.0"]
                 [clj-time "0.8.0"]
                 [ring/ring-devel "1.3.1"]
                 [org.clojure/data.csv "0.1.2"]]

  :profiles {:dev {:source-paths ["dev"]}
             :dependencies [[org.clojure/tools.namespace "0.2.4"]]}

  :aliases {"task" ["run" "-m" "fhirterm.tasks.core"]})
