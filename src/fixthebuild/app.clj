(ns fixthebuild.app
  (:require [integrant.core :as ig]
            [compojure.core :refer :all]
            [ring.middleware.defaults :refer :all]
            [ring.util.response :refer [header resource-response]]))

(defroutes
  app
  (GET "/" []
    (-> (resource-response "index.html" {:root "public"})
        (header "Content-Type" "text/html"))))

(defn- make-handler []
  (wrap-defaults app site-defaults))

(defmethod ig/init-key ::endpoint [_ _]
  (make-handler))
