(ns fhirterm.tasks.util
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as sh]))

(defn make-path [& args]
  (str/join "/" args))

(defn mk-tmp-dir!
  "Creates a unique temporary directory on the filesystem. Typically in /tmp on
  *NIX systems. Returns a File object pointing to the new directory. Raises an
  exception if the directory couldn't be created after 10000 tries."
  []
  (let [base-dir (io/file (System/getProperty "java.io.tmpdir"))
        base-name (str (System/currentTimeMillis) "-" (long (rand 1000000000)) "-")
        tmp-base (str/join "/" [base-dir base-name])
        max-attempts 10000]
    (loop [num-attempts 1]
      (if (= num-attempts max-attempts)
        (throw (Exception. (str "Failed to create temporary directory after " max-attempts " attempts.")))
        (let [tmp-dir-name (str tmp-base num-attempts)
              tmp-dir (io/file tmp-dir-name)]
          (if (.mkdir tmp-dir)
            tmp-dir
            (recur (inc num-attempts))))))))

(defn check-zip-file-is-specified [zip-file file-name-pattern]
  (if (not zip-file)
    (exit 1 (format "You have to specify path to downloaded %1$s file.\nExample: lein task import-loinc ~/Downloads/%1$s"
                    file-name-pattern))

    (when (not (.canRead (io/file zip-file)))
      (exit (format "File %s is not readable!" zip-file) 1))))

(defn unzip-file [zip-path f]
  (let [tmp-path (.getPath (mk-tmp-dir!))]
    (try
      (let [unzip-result (sh/sh "unzip" zip-path "-d" tmp-path)]
        (if (not= 0 (:exit unzip-result))
          (exit 1
                (str "Cannot unzip archive. Do you have unzip utility installed?\n"
                     "Additional information: " (pr-str unzip-result)))

          (do
            (println "Unzipped successfuly")
            (f tmp-path))))

      (finally
        (println "Temp directory removed: " (pr-str (sh/sh "rm" "-rf" tmp-path)))))))
