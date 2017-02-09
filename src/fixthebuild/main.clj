(ns fixthebuild.main
  (:gen-class)
  (:require [integrant.core :as ig]
            [fixthebuild.system :refer [system-config]]))

(defn -main [& args]
  (-> (system-config)
      (assoc-in [:fixthebuild.handler/jetty :join?] true)
      (ig/init)))
