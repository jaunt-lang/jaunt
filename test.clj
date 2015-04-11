(clojure.core/refer-clojure)

(def not-lean-vars #{ ;; "#'neko.context/context" "#'neko.resource/package-name"
                     ;; "#'neko.threading/ui-thread" "#'neko.threading/handler"
                     ;; "#'neko.-utils/keyword->static-field" "#'neko.-utils/keyword->setter"
                     ;; "#'neko.ui.traits/get-display-metrics"
                     })

;; (defn lean-var? [^clojure.lang.Var var]
;;   (let [res (not (not-lean-vars (.toString var)))]
;;     res))

(binding [*lean-compile* true
          *compile-path* "./target-skummet"
          *compiler-options* {:elide-meta [:doc :file :line :added :arglists
                                           :inline
                                           :column :static :author :added :dynamic]
                              :neko.init/release-build true}
          *lean-var?* (fn [x] x)]
  (push-thread-bindings {#'clojure.core/*loaded-libs* (ref (sorted-set))})
  (try
    (clojure.lang.RT/resetID)
    (compile 'clojure.core)
    ;; (compile 'clojure.pprint)
    ;; (compile 'clojure.reflect)
    ;; (compile 'clojure.set)
    (compile 'testskummet.bar)
    (finally (pop-thread-bindings)))

  ;; (clojure.lang.Compiler/compile (io/reader "testskummet/foo.clj") "testskummet/foo.clj" "testskummet/foo.clj" )
  ;; (clojure.lang.Compiler/compile (io/reader "testskummet/bar.clj") "testskummet/bar.clj" "testskummet/bar.clj" )
  )
