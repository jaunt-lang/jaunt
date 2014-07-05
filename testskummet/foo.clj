(ns testskummet.foo)

(def just-value 42)

(defmacro mymacro [b]
  (let [foo :bar]
   `(first ~b)))

(defn ^{:some-meta true
        :more-meta 24
        :and-more [1 2 3]
        :and-even-this {:right ["there"]}
        :and-some-more #{234 :kw}}
  myfn [arg]
  (mymacro arg))
