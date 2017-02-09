(ns fixthebuild.handler
  (:require [integrant.core :as ig]
            [compojure.core :refer [routes]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defmethod ig/init-key ::endpoint
  [_ {:keys [endpoints]}]
  (apply routes endpoints))

(defmethod ig/init-key ::jetty
  [_ {:keys [port join? handler]}]
  (run-jetty handler {:port  port
                      :join? join?}))

(defmethod ig/halt-key! ::jetty
  [_ server]
  (.stop server)
  (.join server))
