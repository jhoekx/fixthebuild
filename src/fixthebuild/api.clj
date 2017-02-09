(ns fixthebuild.api
  (:require [integrant.core :as ig]
            [compojure.core :refer :all]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer :all]))

(defroutes
  api
  (context
    "/api" []
    (GET "/hello" []
         {:status 200
          :body   ["Hello!\r\n"]})))

(defn- make-handler []
  (-> api
      (wrap-json-response)
      (wrap-json-body)
      (wrap-defaults api-defaults)))

(defmethod ig/init-key ::endpoint [_ _]
  (make-handler))
