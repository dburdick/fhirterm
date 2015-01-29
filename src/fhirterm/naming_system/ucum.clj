(ns fhirterm.naming-system.ucum
  (:require [clojure.string :as string]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.java.io :as io]
            [instaparse.core :as insta]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The following code is copied from UCUM-lib library
;; https://github.com/IDEXX/ucum-lib/
;; Copyright (c) 2015 by Dave Kincaid
;; Distributed under the Eclipse Public License either version 1.0.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord Prefix
    [^String code ^String CODE ^String name ^String printSymbol ^Long value])

(defrecord BaseUnit
    [^String code ^String CODE ^String dim ^String name ^String printSymbol ^String property])

(defrecord DerivedUnit
    [^String code ^String CODE ^Boolean isMetric ^String clazz ^String name ^String printSymbol ^String property ^String unit ^String UNIT ^String value])

(def ucum-xml
  (io/resource "ucum-essence.xml"))

(def ucum-bnf
  (io/resource "ucum.ebnf"))

(defn find-tag
  "Given a vector of maps find the first one with the given :tag name"
  [tag maps]
  (first (filter #(= tag (:tag %)) maps)))

(defn prefix
  [element]
  (let [Code (get-in element [:attrs :Code])
        CODE (get-in element [:attrs :CODE])
        content (:content element)
        name (get-in (find-tag :name content) [:content 0])
        printSymbol (get-in (find-tag :printSymbol content) [:content 0])
        value (get-in (find-tag :value content) [:attrs :value])]
    (Prefix. Code CODE name printSymbol value)))

(defn base-unit
  [element]
  (let [Code (get-in element [:attrs :Code])
        CODE (get-in element [:attrs :CODE])
        dim (get-in element [:attrs :dim])
        content (:content element)
        name (get-in (find-tag :name content) [:content 0])
        printSymbol (get-in (find-tag :printSymbol content) [:content 0])
        property (get-in (find-tag :property content) [:content 0])]
    (BaseUnit. Code CODE dim name printSymbol property)))

(defn unit
  [element]
  (let [Code (get-in element [:attrs :Code])
        CODE (get-in element [:attrs :CODE])
        isMetric (get-in element [:attrs :isMetric])
        clazz (get-in element [:attrs :class])
        content (:content element)
        name (get-in (find-tag :name content) [:content 0])
        printSymbol (get-in (find-tag :printSymbol content) [:content 0])
        property (get-in (find-tag :property content) [:content 0])
        value-tag (find-tag :value content)
        Unit (get-in value-tag [:attrs :Unit])
        UNIT (get-in value-tag [:attrs :UNIT])
        value (get-in value-tag [:attrs :value])]
    (DerivedUnit. Code CODE isMetric clazz name printSymbol property Unit UNIT value)))

(defn make-record
  "Create a Prefix, BaseUnit or Unit from the given map."
  [element]
  (case (:tag element)
    :prefix (prefix element)
    :base-unit (base-unit element)
    :unit (unit element)))

(defn read-spec
  "Read in the UCUM spec and create a 3-tuple that contains a list of prefixes, a list of base units and a list of units."
  []
  (let [x (-> (str ucum-xml)
              xml/parse
              zip/xml-zip
              first
              :content)
        recs (map make-record x)]
    [(filter #(= (class %) Prefix) recs)
     (filter #(= (class %) BaseUnit) recs)
     (filter #(= (class %) DerivedUnit) recs)]))

(def units
  "Read in the spec and create the prefixes, base units and derived units."
  (doall (read-spec)))

(def prefixes
  "Var to hold the prefixes."
  (first units))

(def base-units
  "Var to hold the base units."
  (second units))

(def derived-units
  "Var to hold the derived units."
  (nth units 2))

(defn filter-map
  "Get the first map in the list whose :code value matches."
  [l s]
  (first (filter #(= (:code %) s) l)))

(def get-prefix
  "Get the prefix by its code."
  (partial filter-map prefixes))

(def get-base-unit
  "Get the base unit by its code."
  (partial filter-map base-units))

(defn get-unit
  "Get the unit by its code. Returns either a Unit or a BaseUnit."
  [^String s]
  (let [u (filter-map derived-units s)]
    (if (nil? u)
      (get-base-unit s)
      u)))

(defn prefix-symbols
  "The set of prefix symbols in EBNF form"
  []
  (str "PREFIX-SYMBOL = "
       (string/join " | " (map #(str "\"" (:code %) "\"") prefixes))))

(defn atom-symbols
  "The set of atom symbols in EBNF form"
  []
  (str "ATOM-SYMBOL = " (string/join " | " (map #(str "\"" (:code %) "\"") (into base-units derived-units)))))

(defn escape-char
  "Escapes chars for backslash and double quote"
  [c]
  (if (or (= c \")
          (= c \\))
    (str "\\" c)
    (str c)))

(defn annotation-symbols
  "The set of annotation strings in EBNF form"
  []
  (str "<ANNOTATION-STRING> = ("
       (string/join " | " (map #(str "\"" (escape-char (char %)) "\"") (range 33 127)))
       ")+"))

(defn invert-exponent
  "Function to invert the exponent of a unit."
  [s]
  (let [match (re-find #"(\D)(\d+)$" s)]
    (if match
     (str (second match) (* -1 (Integer/valueOf (nth match 2))))
     (str s "-1"))))

(defn grammar
  "Function to read the UCUM EBNF grammar into a string."
  []
  (str (slurp ucum-bnf)
       (atom-symbols) ";\n"
       (prefix-symbols) ";\n"
       (annotation-symbols) ";"))

(def parser
  (insta/parser (grammar)
                :start :main-term
                :input-format :ebnf))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; End of UCUM-lib code
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn lookup-code [{:keys [system] :as params}]
  nil)

(defn filter-codes [filters]
  nil)
