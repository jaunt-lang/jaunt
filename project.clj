(defproject org.jaunt-lang/jaunt (. ^String (slurp "VERSION") (trim))
  :source-paths      ["src/clj"]
  :java-source-paths ["src/jvm" "test/java"]
  :test-paths        ["test/clojure"]
  :resource-paths    ["src/resources"]
  :exclusions        [org.clojure/clojure]
  :profiles {:dev {:plugins [[lein-cljfmt "0.5.0"]]
                   :cljfmt  {:indents {fn*                 [[:inner 0]]
                                       as->                [[:inner 0]]
                                       with-debug-bindings [[:inner 0]]
                                       merge-meta          [[:inner 0]]
                                       add-doc-and-meta    [[:inner 0]]}}}})

