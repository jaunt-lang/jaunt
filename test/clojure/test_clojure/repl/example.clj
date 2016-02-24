(ns clojure.test-clojure.repl.example
  (:require [clojure.string :as s]))

;; sample namespace for repl tests, don't add anything here
(defn foo [])
(defn bar [])
(defn qux [] ::s/foo)
