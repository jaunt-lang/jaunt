;;    Copyright (c) Reid McKenzie. All rights reserved.
;;    The use and distribution terms for this software are covered by the
;;    Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;    which can be found in the file epl-v10.html at the root of this distribution.
;;    By using this software in any fashion, you are agreeing to be bound by
;;    the terms of this license.
;;    You must not remove this notice, or any other, from this software.

(ns clojure.core.compiler
  "EXPERIMENTAL and definitely unstable.

  Compiler and runtime introspection utilities."
  {:authors ["Reid 'arrdem' McKenzie <me@arrdem.com>"]
   :added   "0.2.0"}
  (:import [clojure.lang Compiler Namespace Var PersistentQueue]))

(defn all-vars []
  (->> (all-ns)
       (mapcat (comp vals ns-publics))
       (keep var?)))

(defn uses
  "EXPERIMENTAL

  Returns the use set of a Fn, or of a Var bound to a Fn. The use set of other values is defined to
  the empty set.

  O(1) for all inputs."
  [o]
  (cond (fn? o)  (::uses (meta o) #{})
        (var? o) (recur (deref o))
        :else    #{}))

(defn reaches
  "EXPERIMENTAL

  Returns the reach set of a Fn, or of a Var bound to a Fn. The reach set of other values is defined
  to be the empty set.

  O(N log(N)) on the size of the result set. Iteratively visits each used Var exactly once until
  all reached Vars have been visited and the result set is returned."
  [o]
  (locking Namespace
    (loop [acc                    #{}
           [o & worklist' :as wl] (into PersistentQueue/EMPTY (uses o))]
      (if-not (empty? wl)
        (let [acc' (conj acc o)]
          (recur (into acc' (uses o))
                 (into worklist' (remove acc' (uses o)))))
        acc))))

(defn used-by
  "EXPERIMENTAL

  Returns the set of Vars whose bound values reference the given Var.

  O(N log(N)) on the number of Vars in the system."
  [o]
  (locking Namespace
    (->> (for [ns    (all-ns)
               [_ v] (ns-publics ns)
               :let  [uses (uses v)]
               :when (contains? uses o)]
           v)
         (into #{}))))

(defn reached-by
  "EXPERIMENTAL

  Returns the set of Vars whose bound values reach the given Var.

  O((N log(N))²) on the number of Vars in the system."
  [o]
  (locking Namespace
    (->> (for [ns    (all-ns)
               [_ v] (ns-publics ns)
               :when (contains? (reaches v) o)]
           v)
         (into #{}))))

(defn macro?
  "EXPERIMENTAL

  Attempts to report whether the arguent Object is a macro or not. Unfortunately this determination
  is only possible for Vars. If a Symbol is provided, an attempt will be made to resolve it to a
  Var. If a Var is provided or resolved, the ^:macro flag will be checked and the result
  returned. Any other input will yield false."

  [o]
  (if (symbol? o)
    (recur (resolve o))
    (if (var? o)
      (:macro (meta o) false)
      false)))
