(ns ffcop.config)

(def valid-types ["text" "integer" "boolean" "geometry"])

(def default-fields
  "Default featuretype fields."
  [["id"              "integer"]
   ["the_geom"        "geometry"]
   ["description"     "text"]
   ["default_graphic" "text"]
   ["edit_url"        "text"]])

(def db {:classname "org.postgresql.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/featuretype"
         :user "ffcop"
         :password "magic-unlock"})

(def context-root
  "See README.md for more details.  Expected values are null and /ffcop."
  "/ffcop")

(defmacro R
  "Prepend context-root to path, and prepend strings."
  [path & strs]
  (apply str ffcop.config/context-root path strs))
