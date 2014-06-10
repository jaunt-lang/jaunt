(require '[clojure.java.io :as io])

(import 'clojure.lang.LeanCompiler)

(defn c [f ff]
  (Compiler/compile (io/reader f) ff ff))

(defn cc [f ff]
  (LeanCompiler/compile (io/reader f) ff ff))

(def not-lean-vars
  #{"print-method" "print-dup" "in-ns" "refer"
    "load-file" "load" "-main" "defn" "defmacro" "CollReduce" "InternalReduce" "IKVReduce" "bytes" "chars" "shorts"
    "floats" "ints" "longs" "doubles" "booleans" "parents" "ancestors"
    "coll-reduce" "internal-reduce" "->VecNode" "->ArrayChunk" "->VecSeq" "->Vec" "kv-reduce"
    "as-file" "as-url" "do-copy" "make-reader" "make-writer" "make-input-stream" "make-output-stream"
    "Coercions" "IOFactory"})

(defn lean-var? [^clojure.lang.Var var]
  (and (not (not-lean-vars (.. var sym getName)))
       (not (.isDynamic var))))

(binding [*compile-files* true
          *compile-path* "."
          *compiler-options* {:elide-meta [:doc :file :line :added :arglists :column :static]}
          *lean-var?* lean-var?]
  (cc "src/clj/clojure/core.clj"  "clojure/core.clj")
  (cc "src/clj/clojure/core/protocols.clj" "clojure/core/protocols.clj")
  (cc "src/clj/clojure/instant.clj" "clojure/instant.clj")
  (cc "src/clj/clojure/uuid.clj" "clojure/uuid.clj")
  (cc "src/clj/clojure/string.clj" "clojure/string.clj")
  (cc "src/clj/clojure/java/io.clj" "clojure/java/io.clj")
  (cc "testskummet-src/foo.clj" "testskummet/foo.clj")
  (cc "testskummet-src/bar.clj" "testskummet/bar.clj"))
