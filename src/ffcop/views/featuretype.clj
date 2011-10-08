(ns ffcop.views.featuretype
  (:require [ffcop.views.common :as common])
  (:require [ffcop.database.db :as db])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage "/featuretype" []
         (common/layout
           (unordered-list (db/featuretype-names))))
