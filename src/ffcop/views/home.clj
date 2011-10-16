(ns ffcop.views.home
  (:require [ffcop.views.common :as common])
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(defpage
  "/" []
  (common/layout
    [:h1 "ffcop"]
    [:p "Welcome to ffcop, the Free and Friendly COP."]
    [:dl
     [:dt (link-to "/featuretype" "Feature Type")]
     [:dd "View and edit feature types."]]))
