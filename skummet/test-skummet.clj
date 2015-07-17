(clojure.core/refer-clojure)

(binding [*lean-compile* true
          *compile-path* "./target"
          *compiler-options* {:elide-meta [:doc :file :line :added :arglists
                                           :column :static :dynamic
                                           :author :added :declared :private]
                              :neko.init/release-build true}
          *lean-var?* (fn [x] x)]
  (compile 'skummet-check.one)
  (compile 'skummet-check.hello))
