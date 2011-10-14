(defproject
  ffcop "0.1.0-SNAPSHOT"
  :description "Free and Friendly COP"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/java.jdbc "0.0.6"]
                 [postgresql/postgresql "8.4-702.jdbc4"]
                 [noir "1.2.0"]
                 [hiccup "0.3.6"]]
  :dev-dependencies [[lein-ring "0.4.6"]]
  :ring {:handler ffcop.server/handler}
  :main ffcop.server)
