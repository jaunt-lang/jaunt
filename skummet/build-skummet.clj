(clojure.core/refer-clojure)

(def not-lean-vars #{})

(binding [*lean-compile* true
          *compile-path* "./target"
          *compiler-options* {:elide-meta [:doc :file :line :added :arglists
                                           :inline :declared :private
                                           :column :static :author :added :dynamic]
                              :neko.init/release-build true}
          *lean-var?* (fn [^clojure.lang.Var v] v)]
  (push-thread-bindings {#'clojure.core/*loaded-libs* (ref (sorted-set))})
  (try
    (clojure.lang.RT/resetID)
    (compile 'clojure.core)
    (compile 'skummet-check.one)
    (compile 'skummet-check.hello)
    (finally (pop-thread-bindings))))
