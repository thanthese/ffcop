(ns ffcop.server
  (:require [noir.server :as server])
  ; must explicitly include all view files for uberwar to work
  (:require [ffcop.views.featuretype])
  (:require [ffcop.views.map])
  (:require [ffcop.views.home])
  (:use [hiccup.middleware :only (wrap-base-url)]))

; fix context root problem.  Temporary fix until noir issue is resolved:
;   https://github.com/ibdknox/noir/issues/32
(noir.server.handler/add-custom-middleware wrap-base-url)

; repl entry point
(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'ffcop})))

; ring entry point
(def handler (server/gen-handler {:mode :dev
                                  :ns 'ffcop}))
