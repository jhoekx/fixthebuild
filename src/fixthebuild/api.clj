(ns fixthebuild.api
  (:require [integrant.core :as ig]
            [compojure.core :refer :all]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer :all]))

(defprotocol PersonRepository
  (list-persons [repository])
  (add-person! [repository person])
  (remove-person! [repository uuid]))

(defprotocol Fixer
  (set-fixer! [fixer uuid])
  (get-fixer [fixer]))

(defn- uuid []
  (str (java.util.UUID/randomUUID)))

(defrecord InMemoryPersonRepository [storage]
  PersonRepository
  (list-persons [repository]
    (vals @storage))
  (add-person! [repository person]
    (let [uuid       (or (:uuid person) (uuid))
          new-person (assoc person :uuid uuid)]
      (swap! storage assoc uuid new-person)
      new-person))
  (remove-person! [repository uuid]
    (swap! storage dissoc uuid)))

(defrecord InMemoryFixer [storage]
  Fixer
  (set-fixer! [fixer uuid]
    (reset! storage uuid))
  (get-fixer [fixer]
    @storage))

(defn- ok [response-body]
  {:status 200
   :body   response-body})

(defn- order-persons [repository]
  (->> (list-persons repository)
       (sort-by :uuid)))

(defn- mark-fixer [fixer persons]
  (let [fixer-uuid (get-fixer fixer)]
    (->> persons
         (map (fn [person]
                (if (= fixer-uuid (:uuid person))
                  (assoc person :fixer true)
                  person))))))

(defn- choose-next [current-uuid sorted-uuids]
  (let [next-uuid (->> sorted-uuids
                       (drop-while (fn [test-uuid]
                                     (not (= current-uuid test-uuid))))
                       (second))]
    (if next-uuid
      next-uuid
      (first sorted-uuids))))

(defn api [repository fixer]
  (routes
    (context
      "/api" []
      (GET "/person" []
        (ok
          (->> (order-persons repository)
               (mark-fixer fixer))))
      (POST "/person" {person :body}
        (let [new-person (add-person! repository person)]
          (when (= 1 (count (list-persons repository)))
            (set-fixer! fixer (:uuid new-person)))
          (ok
            new-person)))
      (DELETE "/person/:uuid" [uuid]
        (remove-person! repository uuid)
        {:status 201})
      (POST "/person/:uuid" [uuid]
        (->> (order-persons repository)
             (map :uuid)
             (choose-next uuid)
             (set-fixer! fixer))
        {:status 201}))))

(defn make-handler [repository fixer]
  (-> (api repository fixer)
      (wrap-json-response)
      (wrap-json-body {:keywords? true})
      (wrap-defaults api-defaults)))

(defmethod ig/init-key ::endpoint [_ {:keys [repository fixer]}]
  (make-handler repository fixer))

(defmethod ig/init-key ::memory [_ _]
  (->InMemoryPersonRepository (atom {})))

(defmethod ig/init-key ::fixer [_ _]
  (->InMemoryFixer (atom "")))
