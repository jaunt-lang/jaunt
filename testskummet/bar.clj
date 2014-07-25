(ns testskummet.bar
  (:use [testskummet.foo :only [myfn]])
  (:require clojure.zip)
  (:gen-class))

(def simple-constant 42)

(defn method-using-function [s]
  (.charAt ^String s 3))

(defn ordinary-function [z]
  (method-using-function (myfn z)))

(def function-call-in-def (str @(atom [17 19 21])))

(alter-var-root #'testskummet.foo/just-value + 1500)

(alter-meta! #'simple-constant assoc :nonono 3)

(defmulti my-multi (fn [x y] (type x)))

(defmethod my-multi Long [x y] (+ x y))

(let [someval 40]
  (def inside-var someval))

(definline my-inline
  "Casts to boolean[]"
  {:added "1.1"}
  [xs] `(. clojure.lang.Numbers booleans ~xs))

(defn ^long primitive-function
  {:static true}
  [^long a, ^long b]
  (unchecked-add a b))

(defn -main [& args]
  (println "Value is " (my-multi 500 testskummet.foo/just-value))
  (println "inside-var" 150)
  (println (ordinary-function args))
  (println (rest (conj [1 2 3] 4)))
  (let [x 20, y 10]
    (println (primitive-function x y))))

;; (defn -main [& args]
;;   (dotimes [i 10]
;;     (let [ffn (fn [x] (odd? (count x)))
;;           coll ["quick"]
;;           ;; coll ["quick" "brown" "fox" "jumps" "over" "the" "lazy" "dog"]
;;           ops [map filter remove group-by]
;;           a (System/currentTimeMillis)]
;;       (dotimes [i 10000000]
;;         ((ops (rem i 4)) ffn coll))
;;       (println "It took: " (- (System/currentTimeMillis) a))))
;;   ;; (println (ordinary-function args))
;;   )
