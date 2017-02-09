(ns user
  (:require [integrant.repl :refer [config system prep init go halt clear reset reset-all]]
            [fixthebuild.system :refer [system-config]]))

(integrant.repl/set-prep! system-config)
