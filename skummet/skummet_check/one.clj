(ns skummet-check.one
  (:use [skummet-check.two :only [myfn]])
  (:require [clojure.java.io :as io])
  ;; (:require clojure.pprint)
  ;; (:require [hiccup.core :refer [html]]
  ;;           hiccup.util
  ;;           ;; [clojure.core.async :as a]
  ;;           )
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

(declare forward-declaring-a-function)
(declare forward-declaring-a-function-2)

(defn method-using-function [s]
  (.charAt ^String s 3))

(defn ordinary-function [z]
  (method-using-function (myfn z)))

(def function-call-in-def (count @(atom [17 19 21])))

(alter-meta! #'simple-constant assoc :nonono 3)

(defmulti my-multi (fn [x y] (type x)))

(defmethod my-multi Long [x y] (+ x y))

(let [someval 40]
  (def inside-var someval))

(defn using-forwarded-function []
  (and (= (forward-declaring-a-function) :works)
       (= (forward-declaring-a-function-2) :works)))

(let [hundred 100
      two-hundred 200]
  (defn fun-inside-let [x]
    (+ x hundred two-hundred)))

(let [res :works]
  (defn forward-declaring-a-function []
    res))

(defn forward-declaring-a-function-2 []
  :works)

(definline my-inline
  "Casts to boolean[]"
  {:added "1.1"}
  [xs] `(. clojure.lang.Numbers booleans ~xs))

(defn ^long primitive-function
  ;; {:static true}
  [^long a, ^long b]
  (unchecked-add a b))

(defmacro macro-in-compile-time [& body]
  (when true
    `(println "Testing macroexpansion: " ~(#'myfn body))))

(defprotocol FooProt
  (foofoo [x]))

(extend-protocol FooProt
  String
  (foofoo [x] (count x)))

(defn test-transducers []
  (transduce (comp (filter odd?) (map inc)) + (range 5)))

(defn test-alter-var-root []
  (alter-var-root #'skummet-check.two/just-value + 100)
  skummet-check.two/just-value)

(defn recursive [i]
  (if (= i 0)
    1
    (* (recursive (dec i)) i)))

(def plus-and-str (comp str (partial + 5)))

(defrecord TestRecords [field1 field2])

(let []
  (declare declare1)
  (declare declare2)
  (defn non-top-no-closure-fn [] :works)
  (defn non-top-no-closure-fn2 [] :works))

(let [val 42]
  (defn recursive-fn-with-closure
    ([] (recursive-fn-with-closure 100))
    ([x] (+ x val))))

(defn reflection-test [s]
  (.charAt s 3))

(defonce var-defed-once 42)

(let [cnt (atom 0)]
  (def memoized-fn (memoize (fn []
                              (swap! cnt inc)
                              @cnt))))

(defn test-as-url []
  (= (type (io/as-url "http://google.com")) java.net.URL))

(defn test-spit-and-slurp []
  (let [tmp (java.io.File/createTempFile "skummet-slurp-test" nil)
        data "test skummet"]
    (spit tmp data)
    (assert (= (slurp tmp) data) "spit and slurp don't work")))

(def ^:dynamic *dynamic-var* 1)

(defn test-dynamic-vars []
  (assert (= *dynamic-var* 1))
  (binding [*dynamic-var* 2]
    (assert (= *dynamic-var* 2) "dynamic vars don't work"))
  (assert (= *dynamic-var* 1)))

(defn test-locating-lean-vars []
  (let [v1 (clojure.lang.RT/var "skummet-check.one" "test-as-url")
        v2 (clojure.lang.RT/var "skummet-check.one" "recursive-fn-with-closure")]
    (assert (= (.invoke v1) true) "Locating lean singleton vars doesn't work")
    (assert (= (.invoke v2 10) 52) "Locating lean non-singleton vars doesn't work")))

(gen-class :name TestClass)

(defmacro memoized [inside-defn]
  (let [[_ name & fdecl] inside-defn
        [m fdecl] (if (string? (first fdecl))
                    [{:doc (first fdecl)} (next fdecl)]
                    [{} fdecl])
        [m fdecl] (if (map? (first fdecl))
                    [(conj m (first fdecl)) (next fdecl)]
                    [m fdecl])
        fdecl (if (vector? (first fdecl))
                (list fdecl)
                fdecl)
        [m fdecl] (if (map? (last fdecl))
                    [(conj m (last fdecl)) (butlast fdecl)]
                    [m fdecl])
        m (conj (if (meta name) (meta name) {}) m)]
    `(def ~(with-meta name m)
       (memoize (fn ~@fdecl)))))

(memoized (defn test-redefinition [s] (count s)))

;; FIXME:
;; - Test strict primitives
;; - Test strict Objects
;; - Test recur with strict on both
;; - Test strict closed overs
;; - Test strict fn arguments

(defn ^:strict sum2 [^Iterable xs]
  (let [iter (.iterator xs)]
    (loop [tot (long 0)]
      (if (.hasNext iter)
        (recur (+ tot (long (.next iter))))
        tot))))

;; tests primitive function arguments and loop/recur variables
;; tests that the iter local is type-inferred (strictly) from interop on the xs argument
(defn ^:strict strict-sum ^long [^long init ^Iterable xs]
  (let [iter (.iterator xs)]
    (loop [tot init]
      (if (.hasNext iter)
        (recur (+ tot (long (.next iter))))
        tot))))

;; tests several ^:strict features:
;; - x should be cast immediately, then emitted as a typed local
;; - the lambda should use a typed field for its closed-overs: String x; ISeq xs
;; - the Object-typed return from calling the lambda should force a cast back to String
;; - the call to next should be inlined to c.l.RT/next, thus requiring no cast
(defn ^:strict concat-via-recur [^String x & xs]
  (if xs
    (recur ((fn [] (.concat x ^String (first xs)))) ;; forces cast back to String before recurring
           (next xs))
    x))

;; should use typed fields for both, and toString should need no casting
(deftype ^:strict StrictType [^int x ^String s]
  Object
  (hashCode [this] x)
  (toString [this] (.concat s "-str")))

(defn -main [& args]
  (assert (= (my-multi 10 20) 30) "Multimethods don't work")
  (assert (= skummet-check.two/just-value 42))
  (assert (= (test-alter-var-root) 142) "alter-var-root doesn't work")
  (assert (= inside-var 40) "Internal vars don't work")
  (assert (= (fun-inside-let 42) 342) "defn inside let don't work")
  (println "Testing ordinary function:" (ordinary-function args))
  (assert (= (rest (conj [1 2 3] 4)) '(2 3 4)))
  (let [x 20, y 10]
    (println "Testing primitive functions " (primitive-function x y)))
  (assert (= (test-transducers) 6) "Transducers don't work")
  (assert (= (foofoo "test") 4) "Protocols don't work")
  (assert (= (recursive 5) 120) "Recursive functions don't work")
  (assert (= (plus-and-str 5) "10") "Top-level callable defs don't work")
  ;; (println "Test requiring Clojure namespace" (clojure.set/union #{1} #{2}))
  (set! clojure.core/*warn-on-reflection* true)
  (macro-in-compile-time 1 2 3)
  ;; (clojure.pprint/pprint {:doc "Testing clojure.pprint" :foo 42 :bar 14})
  (assert (= (forward-declaring-a-function) :works) "Forward declarations with `declare` don't work")
  (assert (using-forwarded-function) "Using forward declarations doesn't work")
  (assert (= (non-top-no-closure-fn) :works) "Functions inside empty lets work.")
  (assert (= (recursive-fn-with-closure) 142) "Recursive functions with enclosed values don't work.")
  (assert (= (reflection-test "abcdef") \d) "Reflection doesn't work.")
  (assert (= var-defed-once 42) "defonce doesn't work.")
  (assert (= (do (memoized-fn) (memoized-fn)) 1) "memoize doesn't work.")
  (assert (test-as-url) "protocol functions don't get marked as non-lean")
  (test-spit-and-slurp)
  (test-dynamic-vars)
  (test-locating-lean-vars)
  (assert (= (test-redefinition "foobar") 6) "Redefined functions don't work.")
  (do
    (assert (= 20 (strict-sum 5 [1 2 3 4 5])) "^:strict locals don't work")
    (assert (= "helloworld!" (concat-via-recur "hello" "world" "!")) "^:strict args don't work")
    (let [x (StrictType. 1234 "test")]
      (assert (= [1234 "test-str"] [(hash x) (str x)]) "^:strict deftype doesn't work")))

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
