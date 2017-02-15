(ns fixthebuild.system
  (:require [integrant.core :as ig]
            [fixthebuild.handler]
            [fixthebuild.api]
            [fixthebuild.app]))

(def config
  {:fixthebuild.handler/jetty    {:port    3000
                                  :join?   false
                                  :handler (ig/ref :fixthebuild.handler/endpoint)}
   :fixthebuild.handler/endpoint {:endpoints [(ig/ref :fixthebuild.api/endpoint)
                                              (ig/ref :fixthebuild.app/endpoint)]}
   :fixthebuild.app/endpoint     {}
   :fixthebuild.api/endpoint     {:repository (ig/ref :fixthebuild.api/memory)}
   :fixthebuild.api/memory       {}})

(defn system-config []
  config)
