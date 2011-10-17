(ns ffcop.views.common
  (:require [noir.session :as session])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defn breadcrumbs
  "Make a breadcrumb trail out of a series of crumbs of the form [name
  url].  (The form :none mean do nothing.)  Implicitly adds home."
  [crumbs]
  (when-not (= crumbs :none)
    (let [include-home (conj (seq crumbs) ["Home" "/"])]
      [:span.breakcrumbs (interpose [:span.breadcrumb-separator (h ">")]
                                    (for [[name url] include-home]
                                      (link-to url name)))])))

(defn notifications [flash]
  (when flash
    [:div
     (when (:error flash) [:div.error (:error flash)])
     (when (:msg flash) [:div.notice (:msg flash)])]))

(defpartial
  layout [{:keys [js-list crumb-list header]} & content]
  (html5 [:head
          [:title "ffcop"]
          (include-css "/css/blueprint/screen.css"
                       "/css/blueprint/print.css")
          "<!--[if lt IE 8]>"
          (include-css "/css/blueprint/ie.css")
          "<![endif]-->"
          (include-css "/css/main.css")
          (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js")
          (apply include-js js-list)]
         [:body
          [:div.header [:span.strong "Welcome, "] "Unknown"]
          [:div.container
           (breadcrumbs crumb-list)
           [:h1 header]
           (notifications (session/flash-get))
           content]]))
