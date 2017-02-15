(ns fixthebuild.api
  (:require [integrant.core :as ig]
            [compojure.core :refer :all]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer :all]))

(defprotocol PersonRepository
  (list-persons [repository])
  (add-person! [repository person]))

(defrecord InMemoryPersonRepository [storage]
  PersonRepository
  (list-persons [repository]
    @storage)
  (add-person! [repository person]
    (swap! storage conj person)))

(defn- ok [response-body]
  {:status 200
   :body   response-body})

(defn api [repository]
  (routes
    (context
      "/api" []
      (GET "/person" []
        (ok
          (list-persons repository)))
      (POST "/person" {person :body}
        (add-person! repository person)
        (ok person)))))

(defn make-handler [repository]
  (-> (api repository)
      (wrap-json-response)
      (wrap-json-body {:keywords? true})
      (wrap-defaults api-defaults)))

(defmethod ig/init-key ::endpoint [_ {:keys [repository]}]
  (make-handler repository))

(defmethod ig/init-key ::memory [_ _]
  (->InMemoryPersonRepository (atom [])))
