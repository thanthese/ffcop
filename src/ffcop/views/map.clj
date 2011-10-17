(ns ffcop.views.map
  (:require [ffcop.views.common :as common])
  (:require [ffcop.database.db :as db])
  (:require [noir.session :as session])
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(defpage
  "/map" []
  (html5
    [:head
     [:title "ffcop map"]
     (include-js "/ext-3.4.0/adapter/ext/ext-base.js"
                 "/ext-3.4.0/ext-all.js"
                 "/OpenLayers-2.11/OpenLayers.js")
     (include-css "/GeoExt/resources/css/geoext-all-debug.css"
                  "/ext-3.4.0/resources/css/ext-all.css")
     (include-js "/GeoExt/lib/GeoExt.js"
                 "http://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"
                 "/js/map.js")]
    [:body
     [:div#gxmap]
     [:div#layerlist]]))
