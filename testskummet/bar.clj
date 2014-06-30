(ns testskummet.bar
  (:use testskummet.foo)
  (:gen-class))

(def simple-constant 42)

(defn method-using-function [s]
  (.charAt ^String s 3))

(defn ordinary-function [z]
  (method-using-function (myfn z)))

(def function-call-in-def (str @(atom [17 19 21])))

(alter-var-root #'testskummet.foo/just-value + 1500)

(defn -main [& args]
  (println "Value is " testskummet.foo/just-value)
  (println (ordinary-function args)))
