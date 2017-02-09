(ns fixthebuild.system
  (:require [integrant.core :as ig]
            [fixthebuild.handler]
            [fixthebuild.api]
            [fixthebuild.app]))

(def config
  {:fixthebuild.handler/jetty    {:port    3000
                                  :join?   false
                                  :handler (ig/ref :fixthebuild.handler/endpoint)}
   :fixthebuild.handler/endpoint {:endpoints [(ig/ref :fixthebuild.app/endpoint)
                                              (ig/ref :fixthebuild.api/endpoint)]}
   :fixthebuild.app/endpoint     {}
   :fixthebuild.api/endpoint     {}})

(defn system-config []
  config)
