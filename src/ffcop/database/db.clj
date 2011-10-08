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

(defn featuretype-names []
  (map :tablename (run! "select tablename
                         from pg_tables
                         where schemaname='public'
                           and tablename not in ('spatial_ref_sys',
                                                 'geometry_columns')")))
