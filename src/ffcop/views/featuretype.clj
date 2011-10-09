(ns ffcop.views.featuretype
  (:require [ffcop.views.common :as common])
  (:require [ffcop.database.db :as db])
  (:require [noir.session :as session])
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(def valid-types ["text" "integer" "boolean" "geometry"])

(def default-fields
  [["id"              "integer"]
   ["the_geom"        "geometry"]
   ["description"     "text"]
   ["default_graphic" "text"]
   ["edit_url"        "text"]])

(defn extract-error
  "Extract the human-readable part of the java exception."
  [e]
  (if (.getNextException e)
    (.getMessage (.getNextException e))
    (.getMessage e)))

(defmacro trier
  "General way of handling errors in a defpage.  Dumps a pretty
  message into flash.
  Example:
    (trier '/featuretype/delete' {:ft-name ft-name} (+ 1 2 3))"
  [url opts body]
  `(try ~body
     (catch Exception e#
       (do
         (session/flash-put! {:error (extract-error e#)})
         (render ~url ~opts)))))

(defn notifications [flash]
  [:div
   (when (:error flash) [:div.error (:error flash)])
   (when (:msg flash) [:div.notice (:msg flash)])])

(defn unserialize-fields [fields-string]
  (partition 2 (clojure.string/split fields-string #"\|")))

; list all feature types
(defpage
  [:get "/featuretype"] []
  (let [names (db/featuretype-names)
        num (count names)]
    (common/layout
      [:h1 "Feature Types"]
      (notifications (session/flash-get))
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

; gui for creating a new feature type
(defpage
  "/featuretype/create"
  {:keys [ft-name ft-fields]
   :or {ft-name (db/valid-featuretype-name "untitled")
        ft-fields default-fields}}
  (common/layout
    (include-js "/js/featuretype.js")
    [:h1 "Feature Types / Create"]
    [:p "Default fields have special meaning.  Don't delete them unless you
        know what you're doing."]
    [:p "Valid field names are alphanumeric characters and _."]
    (notifications (session/flash-get))
    (form-to {:onSubmit "return ft.onsubmit()"} [:put "/featuretype"]
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
                  [:td (drop-down "types" valid-types type)]])]]
             [:div.clear (submit-button "Create Feature Type")])))

; action that creates a new featuretype
(defpage
  [:put "/featuretype"] {:keys [ft-name serialized-ft-fields]}
  (let [ft-fields (unserialize-fields serialized-ft-fields)]
    (trier
      "/featuretype/create" {:ft-name ft-name
                             :ft-fields ft-fields}
      (let [final-name (db/create-featuretype ft-name ft-fields)]
        (do
          (session/flash-put!
            {:msg (str "Feature Type " final-name" created.")})
          (render "/featuretype"))))))

; gui for deleting a featuretype
(defpage
  "/featuretype/delete/:ft-name" {:keys [ft-name]}
  (trier
    "/featuretype" nil
    (common/layout
      (include-js "/js/featuretype.js")
      [:h1 "Delete Feature Type " ft-name]
      (notifications (session/flash-get))
      [:p ft-name " has "
       [:span.strong (db/record-count ft-name)] " features."]
      [:p.strong "This operation cannot be undone."]
      (form-to {:onSubmit "return ft.ondelete()"} [:delete "/featuretype"]
               (hidden-field "ft-name" ft-name)
               (submit-button {:class "caution"} "Delete Feature Type"))
      [:table.span-8
       [:tr [:th "Attribute Name"] [:th "Type"]]
       (for [[name type] (db/fields ft-name)]
         [:tr [:td name] [:td type]])])))

; action that deletes a featuretype
(defpage
  [:delete "/featuretype"] {:keys [ft-name]}
  (trier
    "/featuretype" nil
    (do
      (db/delete-table ft-name)
      (session/flash-put! {:msg (str "Feature Type " ft-name
                                     " has been deleted.")})
      (render "/featuretype"))))
