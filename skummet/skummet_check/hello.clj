(ns skummet-check.hello
  (:gen-class
   :overrides-methods ()))

(defn -main [& args]
  (.println ^java.io.PrintStream System/out "Helllooooo!"))
