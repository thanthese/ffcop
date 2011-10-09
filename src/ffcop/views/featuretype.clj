(ns ffcop.views.featuretype
  (:require [ffcop.views.common :as common])
  (:require [ffcop.database.db :as db])
  (:require [noir.session :as session])
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

(def default-fields
  [["id"              "integer"]
   ["the_geom"        "geometry"]
   ["description"     "text"]
   ["default_graphic" "text"]
   ["edit_url"        "text"]])

(defpage
  "/featuretype/create"
  {:keys [ft-name ft-fields]
   :or {ft-name (db/valid-featuretype-name "untitled")
        ft-fields default-fields}}
  (let [flash (session/flash-get)
        error (:error flash)]
    (common/layout
      (include-js "/js/featuretype.js")
      [:h1 "Feature Types / Create"]
      [:p "Default fields have special meaning.  Don't delete them unless you
          know what you're doing."]
      [:p "Valid field names are alphanumeric characters and _."]
      (when error [:div.error error])
      (form-to {:onSubmit "return ft.onsubmit()"} [:put "/featuretype"]
               (hidden-field "serialized-ft-fields")
               (label {:class "important-name"} "" "Feature Type Name")
               (text-field {:class "text"} "ft-name" ft-name)
               [:p
                [:span.fake-button {:onclick "ft.addField()"} "+"]
                " Add field"]
               [:div.span-8.last
                [:table.fields
                 (for [[name type] ft-fields]
                   [:tr.field
                    [:td [:span.fake-button
                          {:onclick "ft.deleteField(this)"}
                          "-"]]
                    [:td (text-field {:class "text name"} name name)]
                    [:td (drop-down "types" valid-types type)]])]]
               [:div.clear (submit-button "Create Feature Type")]))))

(defn unserialize-fields [fields-string]
  (partition 2 (clojure.string/split fields-string #"\|")))

(defn extract-error
  "Extract the human-readable part of the java exception."
  [e]
  (.getMessage (.getNextException e)))

(defpage
  [:put "/featuretype"] {:keys [ft-name serialized-ft-fields]}
  (let [ft-fields (unserialize-fields serialized-ft-fields)]
    (try
      (do
        (db/create-featuretype
          ft-name
          ft-fields)
        "Table added")
      (catch Exception e
        (do
          (session/flash-put! {:error (extract-error e)})
          (render "/featuretype/create"
                  {:ft-name ft-name
                   :ft-fields ft-fields}))))))
