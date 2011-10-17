(ns ffcop.views.featuretype
  (:require [ffcop.config :as config])
  (:require [ffcop.views.common :as common])
  (:require [ffcop.database.db :as db])
  (:require [ffcop.geoserver :as geo])
  (:require [noir.session :as session])
  (:require [noir.response :as resp])
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(defn extract-error
  "Extract the human-readable part of the java exception."
  [e]
  (let [msg (.getMessage (or (.getNextException e)
                             e))]
    (clojure.string/replace msg #" Position: \d*$" "")))

(defmacro on-error
  "On error in body, dump pretty message in flash and perform action.
  The action will probably be either a render or a redirect."
  [action body]
  `(try ~body
     (catch Exception e#
       (do
         (session/flash-put! {:error (extract-error e#)})
         ~action))))

(defn unserialize-fields [fields-string]
  (partition 2 (clojure.string/split fields-string #"\|")))

(defn h-featuretype-fields [fields]
  [:table.span-8
   [:tr [:th "Attribute Name"] [:th "Type"]]
   (for [[name type] fields]
     [:tr [:td name] [:td type]])])

(defn h-featuretype-count [ft-name count]
  [:p
   [:span.strong ft-name] " has "
   [:span.strong count] " features."])

; gui for listing all feature types
(defpage
  "/featuretype" []
  (let [names (db/featuretype-names)]
    (common/layout
      {:header "Feature Types"}
      [:p (link-to "/featuretype/create" "Create") " new feature type."]
      [:p "Displaying " [:span.strong (count names)] " feature types."]
      [:table.span-8
       (for [name names]
         [:tr
          [:td.name name]
          [:td (link-to (str "/featuretype/view/" name) "View")]
          [:td (link-to (str "/featuretype/edit/" name) "Edit")]
          [:td (link-to {:class "caution"}
                        (str "/featuretype/delete/" name) "Delete")]])])))

; gui for viewing one particular feature type
(defpage
  "/featuretype/view/:ft-name" {:keys [ft-name]}
  (on-error
    (resp/redirect "../../featuretype")
    (let [fields (db/fields ft-name)
          count (db/record-count ft-name)]
      (common/layout
        {:crumb-list [["Feature Types" "/featuretype"]]
         :header "View Feature Type"}
        [:p "Viewing feature type " [:span.strong ft-name]]
        (h-featuretype-count ft-name count)
        (h-featuretype-fields fields)))))

; gui for creating a new feature type
(defpage
  "/featuretype/create"
  {:keys [ft-name ft-fields]
   :or {ft-name (db/valid-featuretype-name "untitled")
        ft-fields config/default-fields}}
  (common/layout
    {:js-list ["/js/featuretype.js"]
     :crumb-list [["Feature Types" "/featuretype"]]
     :header "Create Feature Type"}
    [:p "Default fields have special meaning. Don't delete them unless
        you know what you're doing."]
    [:p "Valid field names are alphanumeric characters and _."]
    (form-to {:onSubmit "return ft.onsubmit()"}
             [:put "/featuretype/create"]
             (hidden-field "serialized-ft-fields")
             (label {:class "important-name"} "" "Feature Type Name")
             (text-field {:class "text"} "ft-name" ft-name)
             [:p
              [:span.fake-button {:onclick "ft.addField()"} "+"]
              " Add field"]
             [:div.span-8.last
              [:table.fields
               [:tr [:th ""] [:th "Attribute Name"] [:th "Type"]]
               (for [[name type] ft-fields]
                 [:tr.field
                  [:td [:span.fake-button
                        {:onclick "ft.deleteField(this)"}
                        "-"]]
                  [:td (text-field {:class "text name"} name name)]
                  [:td (drop-down "types" config/valid-types type)]])]]
             [:div.clear (submit-button "Create Feature Type")])))

; action for creating a new featuretype
(defpage
  [:put "/featuretype/create"] {:keys [ft-name serialized-ft-fields]}
  (let [ft-fields (unserialize-fields serialized-ft-fields)]
    (on-error
      (render "/featuretype/create" {:ft-name ft-name
                                     :ft-fields ft-fields})
      (let [final-name (db/create-featuretype! ft-name ft-fields)]
        (do
          (geo/add-featuretype final-name)
          (session/flash-put!
            {:msg [:span
                   "Feature type " [:span.strong final-name]
                   " created."]})
          (resp/redirect "../featuretype"))))))

; gui for deleting a featuretype
(defpage
  "/featuretype/delete/:ft-name" {:keys [ft-name]}
  (on-error
    (resp/redirect "../../featuretype")
    (let [fields (db/fields ft-name)
          count (db/record-count ft-name)]
      (common/layout
        {:js-list ["/js/featuretype.js"]
         :crumb-list [["Feature Types" "/featuretype"]]
         :header "Delete Feature Type"}
        (h-featuretype-count ft-name count)
        [:p.strong "This operation cannot be undone."]
        (form-to {:onSubmit "return ft.ondelete()"}
                 [:delete "/featuretype"]
                 (hidden-field "ft-name" ft-name)
                 (submit-button {:class "caution"} "Delete Feature Type"))
        (h-featuretype-fields fields)))))

; action for deleting featuretype
(defpage
  [:delete "/featuretype"] {:keys [ft-name]}
  (on-error
    (render "/featuretype")
    (do
      (geo/delete-featuretype ft-name)
      (db/delete-featuretype! ft-name)
      (session/flash-put!
        {:msg [:span
               "Feature Type " [:span.strong ft-name]
               " has been deleted."]})
      (render "/featuretype"))))

; gui for editing featuretype
(defpage
  "/featuretype/edit/:ft-name" {:keys [ft-name]}
  (on-error
    (resp/redirect "../../featuretype")
    (let [fields (db/fields ft-name)]
      (common/layout
        {:js-list ["/js/featuretype.js"]
         :crumb-list [["Feature Types" "/featuretype"]]
         :header "Edit Feature Type"}
        [:p "Edit feature type " [:span.strong ft-name] "."]
        [:table.span-8
         [:tr [:th "Attribute Name"] [:th "Type"]]
         (form-to
           [:put (str "/featuretype/edit/" ft-name)]
           [:tr
            [:td (text-field {:class "text"} "name")]
            [:td (drop-down "type" config/valid-types)]
            [:td (submit-button "Add")]])
         (for [[name type] fields]
           [:tr
            [:td name]
            [:td type]
            [:td (link-to (str "/featuretype/field/rename/" ft-name
                               "/" name) "Rename")]
            [:td (link-to (str "/featuretype/field/delete/" ft-name
                               "/" name) "Delete")]])]))))

; action for adding field
(defpage
  [:put "/featuretype/edit/:ft-name"] {:keys [ft-name name type]}
  (on-error
    (render "/featuretype/edit/:ft-name" {:ft-name ft-name})
    (let [new-name (db/add-column! ft-name name type)]
      (do
        (session/flash-put!
          {:msg [:span
                 "Field " [:span.strong new-name]
                 " of type " [:span.strong type]
                 " added to " [:span.strong ft-name] "."]})
        (render "/featuretype/edit/:ft-name" {:ft-name ft-name})))))

; gui for renaming field
(defpage
  "/featuretype/field/rename/:ft-name/:name" {:keys [ft-name name]}
  (common/layout
    {:js-list ["/js/featuretype.js"]
     :crumb-list [["Feature Types" "/featuretype"]
                  ["Edit" (str "/featuretype/edit/" ft-name)]]
     :header "Rename field"}
    [:p
     "Rename field " [:span.strong name]
     " from Feature Type " [:span.strong ft-name] "."]
    (form-to [:post (str "/featuretype/field/rename/" ft-name "/" name)]
             (text-field {:class "text"} "new-name" name)
             (submit-button "Rename field"))))

; action for renaming field
(defpage
  [:post "/featuretype/field/rename/:ft-name/:name"] {:keys [ft-name
                                                             name
                                                             new-name]}
  (on-error
    (render "/featuretype/field/rename/:ft-name/:name"
            {:ft-name ft-name
             :name name})
    (let [final-name (db/rename-column! ft-name name new-name)]
      (do
        (session/flash-put!
          {:msg [:span "Field " [:span.strong name]
                 " renamed to " [:span.strong final-name]
                 " in " [:span.strong ft-name] "."]})
        (resp/redirect (str "../../../../featuretype/edit/" ft-name))))))

; gui for deleting field
(defpage
  "/featuretype/field/delete/:ft-name/:name" {:keys [ft-name name]}
  (on-error
    (resp/redirect (str "../../../../featuretype/edit/" ft-name))
    (let [count (db/field-not-null-count ft-name name)]
      (common/layout
        {:js-list ["/js/featuretype.js"]
         :crumb-list [["Feature Types" "/featuretype"]
                      ["Edit" (str "/featuretype/edit/" ft-name)]]
         :header "Delete field"}
        [:p
         "Delete field " [:span.strong name]
         " from Feature Type " [:span.strong ft-name] "."]
        [:p
         "The feature type " [:span.strong ft-name]
         " has " [:span.strong count]
         " non-null features." ]
        [:p.strong "This operation cannot be undone."]
        (form-to
          {:onSubmit "return ft.ondelete()"}
          [:post (str "/featuretype/field/delete/" ft-name "/" name)]
          (submit-button {:class "caution"} "Delete field"))))))

; action for deleting field
(defpage
  [:post "/featuretype/field/delete/:ft-name/:name"] {:keys [ft-name
                                                             name]}
  (on-error
    (render "/featuretype/field/delete/:ft-name/:name"
            {:ft-name ft-name
             :name name})
    (do
      (db/drop-column! ft-name name)
      (session/flash-put!
        {:msg [:span "Field " [:span.strong name]
                   " deleted from " [:span.strong ft-name] "."]})
      (resp/redirect (str "../../../../featuretype/edit/" ft-name)))))
