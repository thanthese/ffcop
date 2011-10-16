(ns ffcop.views.home
  (:require [ffcop.views.common :as common])
  (:require [ffcop.geoserver :as geo])
  (:require [noir.response :as resp])
  (:require [noir.session :as session])
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(defpage
  "/" []
  (common/layout
    [:h1 "ffcop"]
    (common/h-notifications (session/flash-get))
    [:p "Welcome to ffcop, the " [:em "Free and Friendly COP"] "."]
    [:dl
     [:dt (link-to "/featuretype" "Feature Type")]
     [:dd "View and edit feature types."]]
    [:dl
     [:dt (link-to "/reload" "Reload")]
     [:dd "Reload Geoserver.  Sometimes it helps."]]))

(defpage
  "/reload" []
  (common/layout
    (common/h-breadcrumbs)
    [:h1 "Reload Geoserver"]
    [:p "Sometimes a reload is just the thing to cure what ails ya."]
    (form-to [:post "/reload"]
             (submit-button "Reload Geoserver"))))

(defpage
  [:post "/reload"] []
  (do
    (geo/reload)
    (session/flash-put!
      {:msg "Geoserver reloaded."})
    (resp/redirect ".")))
