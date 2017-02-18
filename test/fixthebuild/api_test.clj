(ns fixthebuild.api-test
  (:use [peridot.core])
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [shrubbery.core :refer :all]
            [fixthebuild.api :refer :all]))

(defn- read-json [response]
  (json/read-str (:body response) :key-fn keyword))

(deftest person-test
  (let [person     {:uuid "id"
                    :name "John Doe"
                    :mail "john@example.org"}
        new-person {:name "John Doe"
                    :mail "john@example.org"}
        repository (mock PersonRepository
                         {:list-persons   [person]
                          :add-person!    person
                          :remove-person! {}})
        handler    (make-handler repository)]
    (testing "Return a list of persons"
      (let [response (-> (session handler)
                         (request "/api/person")
                         :response)]
        (is (= 200 (:status response)))
        (is (= [person] (read-json response)))))
    (testing "Add a person"
      (let [response (-> (session handler)
                         (request "/api/person"
                                  :request-method :post
                                  :content-type "application/json"
                                  :body (json/write-str new-person))
                         :response)]
        (is (= 200 (:status response)))
        (is (received? repository add-person!))
        (is (received? repository add-person! [new-person]))
        (is (= person (read-json response)))))
    (testing "Remove a person"
      (let [response (-> (session handler)
                         (request "/api/person/id"
                                  :request-method :delete)
                         :response)]
        (is (= 201 (:status response)))
        (is (received? repository remove-person!))
        (is (received? repository remove-person! ["id"]))))))
