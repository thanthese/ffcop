(ns ffcop.database.db
  (:require [clojure.java.jdbc :as sql]))

(def db {:classname "org.postgresql.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/featuretype"
         :user "ffcop"
         :password "magic-unlock"})

(defn run [query]
  (sql/with-connection db (sql/with-query-results
                            results [query]
                            (into [] results))))

(defn foo [] (str "My triumph will be " (str (run "select * from example1"))))
