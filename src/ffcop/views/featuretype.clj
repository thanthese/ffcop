(ns ffcop.views.featuretype
  (:require [ffcop.views.common :as common])
  (:require [ffcop.database.db :as db])
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(defpage
  [:get "/featuretype"] []
  (let [names (db/featuretype-names)
        num (count names)]
    (common/layout
      [:h1 "Feature Types"]
      [:p (link-to (str "/featuretype/create") "Create")
       " new feature type."]
      [:p "Displaying " [:span.strong num] " feature types."]
      [:table.span-8
       (for [name names]
         [:tr
          [:td.name name]
          [:td (link-to {:class "caution"}
                        (str "/featuretype/delete/" name)
                        "Delete")]]) ])))

(def types ["geometry" "text" "integer" "boolean"])

(def default-featuretype
  [{:name "the_geom"
    :type "geometry"
    :value "N/A"}
   {:name "description"
    :type "text"
    :value "best field yet!"}
   {:name "count"
    :type "integer"
    :value 42}
   {:name "isCool"
    :type "boolean"
    :value true}])

(defpartial
  featuretype-fields [featuretype]
  (for [{:keys [name type value]} featuretype]
    (cond (= type "geometry") "Geometry is hard"
          (= type "text") [:tr
                           [:td name]
                           [:td value]
                           [:td type]])))

(defpage
  "/featuretype/create" [:as featuretype]
  (let [ft (if (nil? featuretype)
             default-featuretype
             featuretype)]
    (common/layout
      [:h1 "Feature Types / Create"]
      (form-to [:put "/featuretype"]
               (label "testing" "test")
               (text-field "Testing" "Some default")))))
