(ns ffcop.server
  (:require [noir.server :as server])
  ; must explicitly include all view files for uberwar to work
  (:require [ffcop.views.featuretype])
  (:require [ffcop.views.map]))

; repl entry point
(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'ffcop})))

; ring entry point
(def handler (server/gen-handler {:mode :dev
                                  :ns 'ffcop}))
