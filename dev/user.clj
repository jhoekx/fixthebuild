(ns user
  (:require [clojure.test :refer [run-all-tests]]
            [integrant.repl :refer [config system prep init go halt clear reset reset-all]]
            [fixthebuild.system :refer [system-config]]))

(integrant.repl/set-prep! system-config)

(defn test-all []
  (run-all-tests #"fixthebuild.*test$"))

(defn reset-and-test []
  (reset)
  (test-all))
