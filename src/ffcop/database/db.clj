(ns ffcop.database.db
  (:require [clojure.java.jdbc :as sql])
  (:require [ffcop.util :as util])
  (:require [ffcop.config :as config]))

(defn- run
  "Execute query against database, return result set."
  [query]
  (sql/with-connection config/db (sql/with-query-results
                                   results [query]
                                   (into [] results))))

(defn featuretype-names
  "Current featuretype names."
  []
  (map :tablename (run "select tablename
                        from pg_tables
                        where schemaname='public'
                          and tablename not in ('spatial_ref_sys',
                                                'geometry_columns')
                        order by tablename")))

(defn valid-featuretype-name
  "Return root (name) if featuretype name is availble.  Otherwise,
  root_0, root_1, root_2, ... until an available name is found."
  [root]
  (let [names (featuretype-names)]
    (if (util/not-in? root names)
      root
      (first (drop-while #(util/in? % names)
                         (map (partial str root "_")
                              (iterate inc 0)))))))

(defn- enchance-featuretype-fields
  "Fields coming from the views aren't perfect.  Correct and expand on
  them here."
  [fields]
  (for [[name type] fields]
    (let [n (util/legal-chars name)]
      (if (= n "id")
        [n type "primary key"]
        [n type]))))

(defn create-featuretype!
  "Create new table and return name."
  [name fields]
  (let [n (valid-featuretype-name (util/legal-chars name))
        fs (enchance-featuretype-fields fields)]
    (do
      (sql/with-connection config/db (apply (partial sql/create-table n)
                                            fs))
      n)))

(defn delete-table! [tablename]
  (sql/with-connection config/db (sql/drop-table tablename)))

(defn add-column! [tablename fieldname fieldtype]
  (do
    (sql/with-connection
      config/db (sql/do-commands
                  (str "ALTER TABLE " tablename "
                       ADD COLUMN " (util/legal-chars fieldname) " "
                       fieldtype)))))

(defn fields
  "Return table's fields in form:
      [[name1 type1] [name2 type2] ...]"
  [tablename]
  (let [results (run (str "SELECT
                            a.attname AS name,
                            t.typname AS type
                          FROM
                            pg_class c,
                            pg_attribute a,
                            pg_type t
                          WHERE
                            c.relname = '" tablename "'
                            and a.attnum > 0
                            and a.attrelid = c.oid
                            and a.atttypid = t.oid
                          ORDER BY a.attnum;"))]
    (for [{n :name t :type} results]
      [n t])))

(defn record-count [tablename]
  (let [results (run (str "select count(*) from " tablename))]
    (get-in results [0 :count])))
