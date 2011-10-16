(ns ffcop.config)

(def db
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname "//localhost:5432/featuretype"
   :user "ffcop"
   :password "magic-unlock"})

(def geoserver
  {:url "http://localhost/geoserver"
   :workspace "topp"
   :datastore "featuretype"
   :username "admin"
   :password "geoserver"
   :use-credentials true})

(def valid-types ["text" "integer" "boolean"
                  "POINT" "LINESTRING" "POLYGON"])

(def default-fields
  "Default featuretype fields."
  [["id"              "integer"]
   ["the_geom"        "POINT"]
   ["description"     "text"]
   ["default_graphic" "text"]
   ["edit_url"        "text"]])
