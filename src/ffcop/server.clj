(ns ffcop.server
  (:require [noir.server :as server])
  ; must explicitly include all view files for uberwar to work
  (:require [ffcop.views.welcome]))

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'ffcop})))

(def handler (server/gen-handler {:mode :dev
                                  :ns 'ffcop}))
