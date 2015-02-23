(clojure.core/refer-clojure)

(binding [*lean-compile* true
          *compile-path* "./target-skummet"
          *compiler-options* {:elide-meta [:doc :file :line :added ;; :arglists
                                           :column :static
                                           :author :added]
                              :neko.init/release-build true}
          *lean-var?* (fn [x] x)]
  (compile 'testskummet.bar))
