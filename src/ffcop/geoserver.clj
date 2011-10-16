(ns ffcop.geoserver
  (:require [ffcop.config :as config])
  (:use noir.core
        hiccup.core)
  (:import [java.net URI URL HttpURLConnection])
  (:import [java.io OutputStreamWriter]))

(defn- encode-credentials [username password]
  (str "Basic " (.encode (sun.misc.BASE64Encoder.)
                         (.getBytes (str username ":" password)))))

(defn- rest-call
  "Make a rest call to geoserver.  Return the translated response code.
    Example args: '/rest/reload' some-xml 'POST'"
  [url msg verb]
  (let [conn (doto (.openConnection (URL. (str (:url config/geoserver)
                                               url)))
               (.setDoOutput true)
               (.setRequestProperty
                 "Authorization"
                 (encode-credentials (:username config/geoserver)
                                     (:password config/geoserver)))
               (.setRequestProperty "Content-type" "text/xml")
               (.setRequestMethod verb)
               (.connect))]
    (do
      (when msg
        (doto (OutputStreamWriter. (.getOutputStream conn))
          (.write msg)
          (.flush)
          (.close)))
      (let [resp-msg (.getResponseMessage conn)]
        (do
          (.disconnect conn)
          resp-msg)))))

(defn add-featuretype
  "Add featuretype to geoserver. Featuretype *must* already
  exist in the database."
  [name]
  (rest-call (str "/rest/workspaces/" (:workspace config/geoserver)
                  "/datastores/" (:datastore config/geoserver)
                  "/featuretypes")
             (html
               [:featureType
                [:name name]
                [:title name]
                [:nativeCRS "EPSG:4326"]
                [:nativeBoundingBox
                 [:minx "-179"] [:maxx "179"]
                 [:miny "-89"] [:maxy "89"]]])
             "POST"))

(defn delete-featuretype [name]
  (rest-call (str "/rest/layers/"
                  (:workspace config/geoserver)
                  ":" name)
             nil
             "DELETE"))

(defn reload
  "Note: I'm note sure what this does, but nothing worked
  until I did it."
  []
  (rest-call "/rest/reload" nil "POST"))
