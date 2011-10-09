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

(def valid-types ["text" "integer" "boolean" "geometry"])

(def default-featuretype
  [{:name "the_geom"        :type "geometry"}
   {:name "description"     :type "text"}
   {:name "default_graphic" :type "text"}
   {:name "edit_url"        :type "text"}])

(defpage
  "/featuretype/create" [:as featuretype]
  (let [ft (if (empty? featuretype)
             default-featuretype
             featuretype)]
    (common/layout
      [:h1 "Feature Types / Create"]
      (label {:class "important-name"} "" "Feature Type Name")
      (text-field {:class "text"} "featuretype-name"
                  (db/first-available-featuretype-name ))
      (form-to [:put "/featuretype"]
               (hidden-field "featuretype")
               [:p [:span.fake-button "+"] " Add field"]
               [:div.span-8.last
                [:table
                 (for [{:keys [name type]} ft]
                   [:tr
                    [:td [:span.fake-button "-"]]
                    [:td (text-field {:class "text"} name name)]
                    [:td (drop-down "types" valid-types type)]])]]
               [:div.clear (submit-button "Create Feature Type")]))))
