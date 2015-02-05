(ns fhirterm.tasks.import-rxnorm
  (:require [fhirterm.db :as db]
            [honeysql.helpers :as sql]
            [clojure.java.jdbc :as jdbc]
            [fhirterm.tasks.util :refer :all]))


(def rxn-conso-table
  [[:rxcui "varchar(8) not null"]
   [:lat "varchar(3) default 'eng' not null"]
   [:ts "varchar(1)"]
   [:lui "varchar(8)"]
   [:stt "varchar (3)"]
   [:sui "varchar (8)"]
   [:ispref "varchar (1)"]
   [:rxaui "varchar(8) not null"]
   [:saui "varchar (50)"]
   [:scui "varchar (50)"]
   [:sdui "varchar (50)"]
   [:sab "varchar (20) not null"]
   [:tty "varchar (20) not null"]
   [:code "varchar (50) not null"]
   [:str "varchar not null"]
   [:srl "varchar (10)"]
   [:suppress "varchar (1)"]
   [:cvf "varchar(50)"]
   [:csv_column_count_fix "varchar"]])

(def rxn-sty-table
  [[:rxcui "varchar(8) NOT NULL"]
   [:tui "varchar(4)"]
   [:stn "varchar(100)"]
   [:sty "varchar(50)"]
   [:atui "varchar(11)"]
   [:cvf "varchar(50)"]
   [:csv_column_count_fix "varchar"]])

(def rxn-rel-table
  [[:rxcui1    "varchar(8)"]
   [:rxaui1    "varchar(8)"]
   [:stype1    "varchar(50)"]
   [:rel       "varchar(4)"]
   [:rxcui2    "varchar(8)"]
   [:rxaui2    "varchar(8)"]
   [:stype2    "varchar(50)"]
   [:rela      "varchar(100)"]
   [:rui       "varchar(10)"]
   [:srui      "varchar(50)"]
   [:sab       "varchar(20) not null"]
   [:sl        "varchar(1000)"]
   [:dir       "varchar(1)"]
   [:rg        "varchar(10)"]
   [:suppress  "varchar(1)"]
   [:cvf       "varchar(50)"]
   [:csv_column_count_fix "varchar"]])

(def indices ["CREATE INDEX rxn_conso_on_rxcui_idx ON rxn_conso(rxcui)"
              "CREATE INDEX rxn_conso_on_tty_idx ON rxn_conso(tty)"
              "CREATE INDEX rxn_conso_on_code_idx ON rxn_conso(code)"])

(defn- load-rxn-table [tbl-name tbl-def csv-path]
  (jdbc/with-db-transaction [trans db/*db*]
    (db/e! trans (format "DROP TABLE IF EXISTS %s" tbl-name))
    (db/e! trans (apply jdbc/create-table-ddl tbl-name tbl-def))
    (println (format "Created %s table" tbl-name))

    (println (format "Importing data CSV from %s" csv-path))
    (db/e! trans (format "COPY %s FROM '%s' WITH FREEZE CSV DELIMITER AS '|' QUOTE AS E'\\b'"
                         tbl-name csv-path))

    (db/e! trans (format "ALTER TABLE %s DROP COLUMN csv_column_count_fix" tbl-name)))

  (let [c (db/q-val (-> (sql/select [:%count.* :count])
                        (sql/from (keyword tbl-name))))]

    (println (format "Done, imported %d records into %s" c tbl-name))))

(defn- load-rxn-conso-table [tmp-path]
  (load-rxn-table "rxn_conso"
                  rxn-conso-table
                  (make-path tmp-path "rrf" "RXNCONSO.RRF")))

(defn- load-rxn-sty-table [tmp-path]
  (load-rxn-table "rxn_sty"
                  rxn-sty-table
                  (make-path tmp-path "rrf" "RXNSTY.RRF")))

(defn- load-rxn-rel-table [tmp-path]
  (load-rxn-table "rxn_rel"
                  rxn-rel-table
                  (make-path tmp-path "rrf" "RXNREL.RRF")))

(defn- create-indices []
  (doseq [i indices] (db/e! i)))

(defn- perform* [zip-path]
  (unzip-file zip-path
              (fn [tmp-path]
                (load-rxn-conso-table tmp-path)
                (load-rxn-sty-table tmp-path)
                (load-rxn-rel-table tmp-path)
                (create-indices))))

(defn perform [_ args]
  (let [zip-file (first args)]
    (check-zip-file-is-specified zip-file "RxNorm_full_XXXXXXXX.zip")
    (perform* zip-file)))
