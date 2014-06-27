(require '[clojure.java.io :as io])

(import 'clojure.lang.LeanCompiler)

(def not-lean-vars
  #{"in-ns" "refer" "load-file" "load" "-main" "defn" "defmacro" "parents" "ancestors"
    "pr-on" "isa?" "global-hierarchy" ".."
 })

(defn lean-var? [^clojure.lang.Var var]
  (and (not (not-lean-vars (.. var sym getName)))
       (not (.isDynamic var))))

(binding [;; *lean-compile* true
          *compile-path* "./target-skummet"
          *compiler-options* {:elide-meta [:doc :file :line :added :arglists :column :static]}
          *lean-var?* lean-var?]
  (lean-compile 'clojure.core)
  (lean-compile 'clojure.string)
  (lean-compile 'clojure.java.io)
  (lean-compile 'clojure.instant)
  (lean-compile 'clojure.uuid)

  (lean-compile 'clojure.data)
  (lean-compile 'clojure.edn)
  (lean-compile 'clojure.genclass)
  ;; (cc "src/clj/clojure/main.clj"  "clojure/main.clj")
  ;; (cc "src/clj/clojure/pprint.clj"  "clojure/pprint.clj")

  (lean-compile 'testskummet.bar)
  )
