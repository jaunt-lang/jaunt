(ns testskummet.bar
  (:use testskummet.foo)
  (:gen-class))

(def simple-constant 42)

(defn method-using-function [s]
  (.charAt ^String s 3))

(defn ordinary-function [z]
  (method-using-function (myfn z)))

(def function-call-in-def (str @(atom [17 19 21])))

(defn -main [& args]
  (println (ordinary-function args)))
