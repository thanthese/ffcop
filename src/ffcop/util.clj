(ns ffcop.util)

(defn in? [elem coll]
  (some (partial = elem) coll))

(def not-in? (comp not in?))

(defn legal-chars
  "Return forcibly legalized version of text: lower case, spaces to
  underscores, alpha-numeric only."
  [text]
  (-> text
    (clojure.string/lower-case)
    (clojure.string/replace #" " "_")
    (clojure.string/replace #"[^a-z0-9_]" "")))
