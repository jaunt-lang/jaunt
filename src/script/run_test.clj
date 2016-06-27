(System/setProperty "java.awt.headless" "true")

(require '[clojure.test :as test]
         <<<<<<< Updated upstream
         '[clojure.tools.namespace.find :as ns]
         'clojure.repl)

(def namespaces
  (remove (read-string (System/getProperty "clojure.test-clojure.exclude-namespaces"))
          (ns/find-namespaces-in-dir (java.io.File. "test"))))
=======
'[clojure.tools.namespace.find :as ns])

(def namespaces
  (remove
   (read-string
    (System/getProperty "clojure.test-clojure.exclude-namespaces"))
   (ns/find-namespaces-in-dir
    (java.io.File. "test"))))
>>>>>>> Stashed changes

(def empty-summary
  {:type  :summary
   :test  0
   :pass  0
   :fail  0
   :error 0})

(defn merge-summary [l r]
  (let [keys [:test :pass :fail :error]
        l'   (select-keys l keys)
        r'   (select-keys r keys)]
    (-> (merge-with + l' r')
        (assoc :type :summary))))

(def color-codes
  {:reset             "\u001b[0m"
   :bright            "\u001b[1m"
   :blink-slow        "\u001b[5m"
   :underline         "\u001b[4m"
   :underline-off     "\u001b[24m"
   :inverse           "\u001b[7m"
   :inverse-off       "\u001b[27m"
   :strikethrough     "\u001b[9m"
   :strikethrough-off "\u001b[29m"

   :fg/default "\u001b[39m"
   :fg/white   "\u001b[37m"
   :fg/black   "\u001b[30m"
   :fg/red     "\u001b[31m"
   :fg/green   "\u001b[32m"
   :fg/blue    "\u001b[34m"
   :fg/yellow  "\u001b[33m"
   :fg/magenta "\u001b[35m"
   :fg/cyan    "\u001b[36m"

   :bg/default "\u001b[49m"
   :bg/white   "\u001b[47m"
   :bg/black   "\u001b[40m"
   :bg/red     "\u001b[41m"
   :bg/green   "\u001b[42m"
   :bg/blue    "\u001b[44m"
   :bg/yellow  "\u001b[43m"
   :bg/magenta "\u001b[45m"
   :bg/cyan    "\u001b[46m"})

(defn colorize [num color]
  (if (zero? num)
    (str num)
    (str (get color-codes color color) num (get color-codes :reset))))

(defmethod test/report :summary [m]
  (test/with-test-out
    (println "\nRan" (colorize (:test m) :fg/green) "tests containing"
             (+ (:pass m) (:fail m) (:error m)) "assertions.")
    (println (colorize (:fail m) :fg/yellow) "failures,"
             (colorize (:error m) :fg/red) "errors.")))

(defn test-vars [ns]
  (keep (comp :test meta) (vals (ns-interns (the-ns ns)))))

(defn has-tests? [ns]
  (not-empty (test-vars ns)))

(loop [acc               empty-summary
       [ns & namespaces] namespaces]
  (if (nil? ns)
    (do (println "\n----------------------------------------\nGrand total:")
        (test/report acc)
        (let [ok?    (test/successful? acc)
              status (if ok? 0 -1)]
          (println ok? status)
          (System/exit status)))
    (let [{:keys [fail error]
           :as   res} (try (require ns)
                           (if (has-tests? ns)
                             (let [summary (test/run-tests ns)]
                               (test/report summary)
                               (merge-summary acc summary))
                             (do (printf "Warning: namespace %s%s%s has no tests!\n"
                                         (color-codes :fg/yellow) (name ns) (color-codes :reset))
                                 acc))
                           (catch Exception e
                             (.printStackTrace e)
                             (merge-summary acc {:error 1})))]
      (if (zero? (+ fail error))
        (recur res namespaces)
        (System/exit -1)))))
