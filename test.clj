(require '[clojure.java.io :as io])

(import 'clojure.lang.LeanCompiler)

(def not-lean-vars #{"#'clojure.core/in-ns" "#'clojure.core/refer"
                     "#'clojure.core/load-file" "#'clojure.core/load"
                     "#'clojure.core/defn" "#'clojure.core/defmacro" "#'clojure.core/parents"
                     "#'clojure.core/ancestors" "#'clojure.core/pr-on" "#'clojure.core/isa?"
                     "#'clojure.core/global-hierarchy"
                     "#'clojure.core/.." "#'neko.context/context"
                     "#'neko.resource/package-name" "#'neko.threading/ui-thread" "#'neko.threading/handler"
                     "#'neko.-utils/keyword->static-field" "#'neko.-utils/keyword->setter"
                     "#'neko.ui.traits/get-display-metrics"
                     })

(defn lean-var? [^clojure.lang.Var var]
  (let [res (and (not (not-lean-vars (.toString var)))
              (not (.startsWith (.. var sym getName) "-")))]
    res))

(binding [*lean-compile* true
          *compile-path* "./target-skummet"
          *compiler-options* {:elide-meta [:doc :file :line :added :arglists :column :static]}
          *lean-var?* lean-var?]
  (force-compile 'clojure.core)
  (force-compile 'clojure.string)
  (force-compile 'clojure.java.io)
  (force-compile 'clojure.instant)
  (force-compile 'clojure.uuid)

  (force-compile 'clojure.data)
  (force-compile 'clojure.edn)
  (force-compile 'clojure.genclass)
  (force-compile 'clojure.main)
  (force-compile 'clojure.pprint)
  (force-compile 'clojure.set)
  (force-compile 'clojure.stacktrace)
  (force-compile 'clojure.walk)
  (force-compile 'clojure.zip)
  (force-compile 'clojure.xml)

  (force-compile 'testskummet.bar)
  )
