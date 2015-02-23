(ns testskummet.bar
  (:use [testskummet.foo :only [myfn]])
  ;; (:require [hiccup.core :refer [html]]
  ;;           hiccup.util
  ;;           ;; [clojure.core.async :as a]
  ;;           )
  ;; (:require testskummet.classify)
  (:import
   (java.io Reader InputStream InputStreamReader PushbackReader
            BufferedReader File OutputStream
            OutputStreamWriter BufferedWriter Writer
            FileInputStream FileOutputStream ByteArrayOutputStream
            StringReader ByteArrayInputStream
            BufferedInputStream BufferedOutputStream
            CharArrayReader Closeable)
   (java.net URI URL MalformedURLException Socket URLDecoder URLEncoder))
  (:gen-class
   :overrides-methods ()))

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

(defprotocol FooProt
  (foofoo [x]))

(extend-protocol FooProt
  String
  (foofoo [x] (str "fofo" x)))

(defn test-transducers []
  (println (transduce (comp (filter odd?) (map inc)) + (range 5))))

(defn -main [& args]
  (println "Value is " (my-multi 500 testskummet.foo/just-value))
  (println "inside-var" 150)
  (println (ordinary-function args))
  (println (rest (conj [1 2 3] 4)))
  (let [x 20, y 10]
    (println (primitive-function x y)))
  (test-transducers)
  (println (foofoo "omg"))
  (set! clojure.core/*warn-on-reflection* true)
  (println "Hello world")
  ;; (let [h [:span {:class "foo"} "bar"]]
  ;;     (println (html h)))
  ;; (println (hiccup.util/as-str 100 200 300))
  ;; (println  @clojure.core.async.impl.dispatch/executor)
  ;; (println clojure.core.async.impl.exec.threadpool/the-executor)
  ;; (clojure.core.async.impl.dispatch/run #(println "RUNNING FROM EXECUTOR!"))
  ;; (let [c (a/chan)]
  ;;   (a/put! c (first args))
  ;;   (a/go (println "answer is" (a/<! c))))
  ;; (Thread/sleep 1)
  ;; (println (keep (fn [[_ v]] (if (var? v) v)) (.getMappings (find-ns 'clojure.core))))
  )

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
