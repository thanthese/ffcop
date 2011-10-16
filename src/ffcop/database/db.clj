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

(defn- do-command!
  "Execute a single 'do something' query against the database.
  Concatenates multiple parameters into a single command/str.
  Also makes those pesky spaces unnecessary.
    Example: (do-command 'drop table' some-table)"
  [& command]
  (sql/with-connection
    config/db
    (sql/do-commands
      (apply str (interpose " " command)))))

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
  them here.  For example, make 'id' a primary key."
  [fields]
  (for [[name type] fields]
    (let [n (util/legal-chars name)]
      (if (= n "id")
        [n type "primary key"]
        [n type]))))

(defn create-featuretype!
  "Create new table and return name."
  [name fields]
  (let [nm (valid-featuretype-name (util/legal-chars name))
        flds (enchance-featuretype-fields fields)]
    (do
      (sql/with-connection config/db (apply (partial sql/create-table nm)
                                            flds))
      nm)))

(defn delete-table! [tablename]
  (sql/with-connection config/db (sql/drop-table tablename)))

(defn add-column!
  "Add column to table, return the (possibly corrected) fieldname."
  [tablename fieldname fieldtype]
  (let [valid-fieldname (util/legal-chars fieldname)]
    (do
      (do-command! "ALTER TABLE" tablename
                   "ADD COLUMN" valid-fieldname
                   fieldtype)
      valid-fieldname)))

(defn rename-column!
  "Rename column in table, return the (possibly corrected) new name."
  [tablename old-name new-name]
  (let [valid-name (util/legal-chars new-name)]
    (do (do-command! "ALTER TABLE" tablename
                     "RENAME COLUMN" (util/legal-chars old-name)
                     "TO" valid-name)
      valid-name)))

(defn drop-column! [tablename column-name]
  (do-command! "ALTER TABLE" tablename
               "DROP COLUMN" (util/legal-chars column-name)))

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


(defn field-not-null-count [tablename field]
  (let [results (run (str "select count(*) from " tablename
                          " where " field " is not null"))]
    (get-in results [0 :count])))
