(ns fixthebuild.api
  (:require [integrant.core :as ig]
            [compojure.core :refer :all]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer :all]))

(defprotocol PersonRepository
  (list-persons [repository])
  (add-person! [repository person])
  (remove-person! [repository uuid]))

(defn- uuid []
  (str (java.util.UUID/randomUUID)))

(defrecord InMemoryPersonRepository [storage]
  PersonRepository
  (list-persons [repository]
    (vals @storage))
  (add-person! [repository person]
    (let [uuid (uuid)
          new-person (assoc person :uuid uuid)]
      (swap! storage assoc uuid new-person)))
  (remove-person! [repository uuid]
    (swap! storage dissoc uuid)))

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
        (ok
          (add-person! repository person)))
      (DELETE "/person/:uuid" [uuid]
        (remove-person! repository uuid)
        {:status 201}))))

(defn make-handler [repository]
  (-> (api repository)
      (wrap-json-response)
      (wrap-json-body {:keywords? true})
      (wrap-defaults api-defaults)))

(defmethod ig/init-key ::endpoint [_ {:keys [repository]}]
  (make-handler repository))

(defmethod ig/init-key ::memory [_ _]
  (->InMemoryPersonRepository (atom {})))
