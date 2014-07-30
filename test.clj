(def not-lean-vars #{"#'neko.context/context" "#'neko.resource/package-name"
                     "#'neko.threading/ui-thread" "#'neko.threading/handler"
                     "#'neko.-utils/keyword->static-field" "#'neko.-utils/keyword->setter"
                     "#'neko.ui.traits/get-display-metrics"
                     })

(defn lean-var? [^clojure.lang.Var var]
  (let [res (not (not-lean-vars (.toString var)))]
    res))

(binding [*lean-compile* true
          *compile-path* "./target-skummet"
          *compiler-options* {:elide-meta [:doc :file :line :added ;; :arglists
                                           :column ;; :static
                                           :author :added]
                              :neko.init/release-build true}
          *lean-var?* lean-var?]
  (push-thread-bindings {#'clojure.core/*loaded-libs* (ref (sorted-set))})
  (try
    (compile 'clojure.core)
    (compile 'testskummet.bar)
    (finally (pop-thread-bindings)))
  ;; (force-compile 'clojure.string)
  ;; (force-compile 'clojure.java.io)
  ;; (force-compile 'clojure.uuid)
  ;; (force-compile 'clojure.instant)

  ;; (clojure.lang.Compiler/compile (io/reader "testskummet/foo.clj") "testskummet/foo.clj" "testskummet/foo.clj" )
  ;; (clojure.lang.Compiler/compile (io/reader "testskummet/bar.clj") "testskummet/bar.clj" "testskummet/bar.clj" )
  ;; (force-compile 'org.bytopia.skummtest.main)
  )
