(ns ffcop.database.db
  (:require [clojure.java.jdbc :as sql]))

(def db {:classname "org.postgresql.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/featuretype"
         :user "ffcop"
         :password "magic-unlock"})

(defn run!
  "Execute query against database, return result set."
  [query]
  (sql/with-connection db (sql/with-query-results
                            results [query]
                            (into [] results))))

(defn in? [elem coll]
  (some (partial = elem) coll))

(defn not-in? [& args]
  (not (apply in? args)))

(defn featuretype-names []
  (map :tablename (run! "select tablename
                         from pg_tables
                         where schemaname='public'
                           and tablename not in ('spatial_ref_sys',
                                                 'geometry_columns')")))

(defn first-available-featuretype-name []
  (let [names (featuretype-names)]
    (first (drop-while #(in? % names)
                       (map (partial str "untitled_")
                            (iterate inc 0))))))
