(ns fhirterm.tasks.util
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn exit [status msg]
  (println msg)
  (System/exit status))

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
