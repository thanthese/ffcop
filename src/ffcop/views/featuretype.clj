(ns ffcop.views.featuretype
  (:require [ffcop.config :as config])
  (:require [ffcop.views.common :as common])
  (:require [ffcop.database.db :as db])
  (:require [noir.session :as session])
  (:require [noir.response :as resp])
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(defn extract-error
  "Extract the human-readable part of the java exception."
  [e]
  (.getMessage (or (.getNextException e)
                   e)))

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

(defn h-breadcrumbs
  "Make a breadcrumb trail out of a series of crumbs of the form
  [name url].  Implicitly adds home."
  [& crumbs]
  (let [with-root (conj crumbs ["Home" "/"])]
    [:span.breakcrumbs (interpose [:span.breadcrumb-separator (h ">")]
                                  (for [[name url] with-root]
                                    (link-to url name)))]))

(defn h-notifications [flash]
  [:div
   (when (:error flash) [:div.error (:error flash)])
   (when (:msg flash) [:div.notice (:msg flash)])])

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
      [:h1 "Feature Types"]
      (h-notifications (session/flash-get))
      [:p (link-to (str "/featuretype/create") "Create")
       " new feature type."]
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
        (h-breadcrumbs ["Feature Types" "/featuretype"])
        [:h1 "View Feature Type " ft-name]
        (h-featuretype-count ft-name count)
        (h-featuretype-fields fields)))))

; gui for creating a new feature type
(defpage
  "/featuretype/create"
  {:keys [ft-name ft-fields]
   :or {ft-name (db/valid-featuretype-name "untitled")
        ft-fields config/default-fields}}
  (common/layout
    (include-js "/js/featuretype.js")
    (h-breadcrumbs ["Feature Types" "/featuretype"])
    [:h1 "Feature Types / Create"]
    [:p "Default fields have special meaning. Don't delete them unless
        you know what you're doing."]
    [:p "Valid field names are alphanumeric characters and _."]
    (h-notifications (session/flash-get))
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
          (session/flash-put!
            {:msg (str "Feature Type " final-name" created.")})
          (resp/redirect "../featuretype"))))))

; gui for deleting a featuretype
(defpage
  "/featuretype/delete/:ft-name" {:keys [ft-name]}
  (on-error
    (resp/redirect "../../featuretype")
    (let [fields (db/fields ft-name)
          count (db/record-count ft-name)]
      (common/layout
        (include-js "/js/featuretype.js")
        (h-breadcrumbs ["Feature Types" "/featuretype"])
        [:h1 "Delete Feature Type " ft-name]
        (h-notifications (session/flash-get))
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
      (db/delete-table! ft-name)
      (session/flash-put!
        {:msg (str "Feature Type " ft-name " has been deleted.")})
      (render "/featuretype"))))

; gui for editing featuretype
(defpage
  "/featuretype/edit/:ft-name" {:keys [ft-name]}
  (on-error
    (resp/redirect "../../featuretype")
    (let [fields (db/fields ft-name)]
      (common/layout
        (include-js "/js/featuretype.js")
        (h-breadcrumbs ["Feature Types" "/featuretype"]
                       ["View" (str "/featuretype/view/" ft-name)])
        [:h1 "Edit Feature Type " ft-name]
        (h-notifications (session/flash-get))
        [:table.span-8
         [:tr [:th "Attribute Name"] [:th "Type"]]
         (form-to
           [:put (str "/featuretype/edit/" ft-name)]
           [:tr
            [:td (text-field "name")]
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
    (do
      (db/add-column! ft-name name type)
      (session/flash-put!
        {:msg [:span
               "Field " [:span.strong name]
               " of type " [:span.strong type]
               " added to " [:span.strong name] "."]})
      (render "/featuretype/edit/:ft-name" {:ft-name ft-name}))))

;; STUB
; gui for deleting field
(defpage
  "/featuretype/field/delete/:ft-name/:name" {:keys [ft-name name]}
  (common/layout
    (include-js "/js/featuretype.js")
    (h-breadcrumbs ["Feature Types" "/featuretype"]
                   ["Edit" (str "/featuretype/edit" ft-name)])
    [:h1 "Delete field " name " from Feature Type " ft-name]
    (h-notifications (session/flash-get))))
