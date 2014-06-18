(require '[clojure.java.io :as io])

(import 'clojure.lang.LeanCompiler)

(defn c [f ff]
  (Compiler/compile (io/reader f) ff ff))

(defn cc [f ff]
  (LeanCompiler/compile (io/reader f) ff ff))

(def not-lean-vars
  #{"in-ns" "refer" "load-file" "load" "-main" "defn" "defmacro" "parents" "ancestors"})

(defn lean-var? [^clojure.lang.Var var]
  (and (not (not-lean-vars (.. var sym getName)))
       (not (.isDynamic var))))

(binding [*lean-compile* true
          *compile-path* "./target-skummet"
          *compiler-options* {:elide-meta [:doc :file :line :added :arglists :column :static]}
          *lean-var?* lean-var?]
  (compile 'clojure.core)
  (cc "src/clj/clojure/string.clj" "clojure/string.clj")
  (cc "src/clj/clojure/java/io.clj" "clojure/java/io.clj")
  ;; (compile 'clojure.instant)
  ;; (compile 'clojure.uuid)

  ;; (cc "src/clj/clojure/data.clj"  "clojure/data.clj")
  ;; (cc "src/clj/clojure/edn.clj"  "clojure/edn.clj")
  ;; (cc "src/clj/clojure/genclass.clj"  "clojure/genclass.clj")
  ;; (cc "src/clj/clojure/main.clj"  "clojure/main.clj")
  ;; (cc "src/clj/clojure/pprint.clj"  "clojure/pprint.clj")

  (compile 'testskummet.bar)
  )
