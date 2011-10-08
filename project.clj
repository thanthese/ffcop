(defproject ffcop "0.1.0-SNAPSHOT"
            :description "Free and Friendly COP"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [noir "1.2.0"]]
            :dev-dependencies [[lein-ring "0.4.6"]]
            :ring {:handler ffcop.server/handler}
            :main ffcop.server)

