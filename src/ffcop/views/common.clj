(ns ffcop.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpartial
  layout [& content]
  (html5 [:head
          [:title "ffcop"]
          (include-css "/css/blueprint/screen.css")
          (include-css "/css/blueprint/print.css")
          "<!--[if lt IE 8]>"
          (include-css "/css/blueprint/ie.css")
          "<![endif]-->"
          (include-css "/css/main.css")
          (include-js  "http://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js")]
         [:body
          [:div.header [:span.strong "Welcome, "] "Unknown"]
          [:div.container content]]))
