(ns fhirterm.csv
  (:refer-clojure :exclude [read])
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:import  [java.io StringWriter]))

(defn write [maps]
  (let [writer (StringWriter.)]
    (csv/write-csv writer (map vals maps))
    (str writer)))

(defn read [in & [options]]
  (csv/read-csv in
                :separator (or (:separator options) \,)
                :quote (or (:quote options) \")))

(defn read-file [filename options row-fn]
  (with-open [in-file (io/reader filename)]
    (let [rowsfn (if (:skip-first-row options)
                   rest
                   identity)]
      (doseq [row (rowsfn (read in-file (dissoc options :skip-first-row)))]
        (row-fn row)))))
