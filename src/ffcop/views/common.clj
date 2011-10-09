(ns ffcop.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(def blueprint "
  <link rel=\"stylesheet\" href=\"/css/blueprint/screen.css\" type=\"text/css\" media=\"screen, projection\">
  <link rel=\"stylesheet\" href=\"/css/blueprint/print.css\" type=\"text/css\" media=\"print\">
  <!--[if lt IE 8]>
    <link rel=\"stylesheet\" href=\"/css/blueprint/ie.css\" type=\"text/css\" media=\"screen, projection\">
  <![endif]-->
  <link rel=\"stylesheet\" href=\"/css/main.css\" type=\"text/css\" media=\"screen, projection\">
 ")

(defpartial layout [& content]
            (html5 [:head [:title "ffcop"] blueprint]
                   [:body
                    [:div.header [:span.strong "Welcome, "] "Unknown"]
                    [:div.container content]]))
