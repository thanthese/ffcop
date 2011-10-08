(ns ffcop.views.welcome
  (:require [ffcop.views.common :as common])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to ffcop"]))
