(require '[clojure.java.io :as io])

(import 'clojure.lang.LeanCompiler)

(def not-lean-vars #{"#'clojure.core/in-ns" "#'clojure.core/refer"
                     "#'clojure.core/load-file" "#'clojure.core/load"
                     "#'clojure.core/defn" "#'clojure.core/defmacro" "#'clojure.core/parents"
                     "#'clojure.core/ancestors" "#'clojure.core/pr-on" "#'clojure.core/isa?"
                     "#'clojure.core/global-hierarchy" "#'clojure.core/.."})

(defn lean-var? [^clojure.lang.Var var]
  (and (not (not-lean-vars (.toString var)))
       (not (.startsWith (.. var sym getName) "-"))))

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
