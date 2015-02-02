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

(def indices ["CREATE INDEX rxn_conso_on_rxcui_idx ON rxn_conso(rxcui)"
              "CREATE INDEX rxn_conso_on_tty_idx ON rxn_conso(tty)"
              "CREATE INDEX rxn_conso_on_code_idx ON rxn_conso(code)"])

(defn- load-rxn-conso [csv-path]
  (jdbc/with-db-transaction [trans db/*db*]
    (db/e! trans (format "DROP TABLE IF EXISTS rxn_conso"))
    (db/e! trans (apply jdbc/create-table-ddl "rxn_conso" rxn-conso-table))
    (println (format "Created rxn_conso table"))

    (println "Uploading RxNorm Conso table")
    (db/e! trans (format "COPY rxn_conso FROM '%s' WITH FREEZE CSV DELIMITER AS '|' QUOTE AS E'\\b'"
                   csv-path))

    (doseq [i indices] (db/e! trans i))

    (db/e! trans "ALTER TABLE rxn_conso DROP COLUMN csv_column_count_fix"))

  (let [c (db/q-val (-> (sql/select [:%count.* :count])
                        (sql/from :rxn_conso)))]
    (println (format "Done, imported %d RxNorm records" c))))

(defn- perform* [zip-path]
  (unzip-file zip-path
              (fn [tmp-path]
                (load-rxn-conso (make-path tmp-path "rrf" "RXNCONSO.RRF")))))

(defn perform [_ args]
  (let [zip-file (first args)]
    (check-zip-file-is-specified zip-file "RxNorm_full_XXXXXXXX.zip")
    (perform* zip-file)))
