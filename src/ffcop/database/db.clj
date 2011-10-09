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

(defn featuretype-names
  "Current featuretype names."
  []
  (map :tablename (run! "select tablename
                         from pg_tables
                         where schemaname='public'
                           and tablename not in ('spatial_ref_sys',
                                                 'geometry_columns')")))

(defn valid-featuretype-name
  "Return root (name) if featuretype name is availble.  Otherwise,
  root_0, root_1, root_2, ... until an available name is found."
  [root]
  (let [names (featuretype-names)]
    (if (not-in? root names)
      root
      (first (drop-while #(in? % names)
                         (map (partial str root "_")
                              (iterate inc 0)))))))

(defn legal-chars
  "Return forcibly legalized version of text: lower case, spaces to
  underscores, alpha-numeric only."
  [text]
  (-> text
    (clojure.string/lower-case)
    (clojure.string/replace #" " "_")
    (clojure.string/replace #"[^a-z0-9_]" "")))

(defn enchance-featuretype-fields
  "Fields coming from the views aren't perfect.  Correct and expand on
  them here."
  [fields]
  (for [[name type] fields]
    (let [n (legal-chars name)]
      (if (= n "id")
        [n type "primary key"]
        [n type]))))

(defn create-featuretype [name fields]
  (let [n (valid-featuretype-name (legal-chars name))
        fs (enchance-featuretype-fields fields)]
    (sql/with-connection db (apply (partial sql/create-table n) fs))))
