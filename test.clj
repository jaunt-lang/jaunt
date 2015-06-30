(clojure.core/refer-clojure)

(def not-lean-vars #{;; "#'neko.-utils/keyword->static-field" "#'neko.-utils/keyword->setter"
                     ;; "#'neko.ui.traits/get-display-metrics"
                     })

;; (defn lean-var? [^clojure.lang.Var var]
;;   (let [res (not (not-lean-vars (.toString var)))]
;;     res))

(binding [*lean-compile* true
          *compile-path* "./target-skummet"
          *compiler-options* {:elide-meta [:doc :file :line :added :arglists
                                           :inline :declared :private
                                           :column :static :author :added :dynamic]
                              :neko.init/release-build true}
          *lean-var?* (fn [x] x)]
  (push-thread-bindings {#'clojure.core/*loaded-libs* (ref (sorted-set))})
  (try
    (clojure.lang.RT/resetID)
    (compile 'clojure.core)
    (compile 'testskummet.bar)
    (compile 'testskummet.hello)
    (finally (pop-thread-bindings))))
