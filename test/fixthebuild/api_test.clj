(ns fixthebuild.api-test
  (:use [peridot.core])
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [shrubbery.core :refer :all]
            [fixthebuild.api :refer :all]))

(defn- read-json [response]
  (json/read-str (:body response) :key-fn keyword))

(def new-person {:name "John Doe"
                 :mail "john@example.org"})

(def person {:uuid "abc"
             :name "John Doe"
             :mail "john@example.org"})

(def next-person {:uuid "bcd"
                  :name "Joe Smith"
                  :mail "joe@example.org"})

(deftest person-test
  (let [repository (mock PersonRepository
                         {:list-persons   [next-person person]
                          :add-person!    person
                          :get-person     person
                          :remove-person! {}
                          :update-person! {}})
        fixer      (mock Fixer)
        handler    (make-handler repository fixer)]
    (testing "Return a list of persons ordered by uuid"
      (let [response (-> (session handler)
                         (request "/api/person")
                         :response)]
        (is (= 200 (:status response)))
        (is (= [person next-person] (read-json response)))))

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
                         (request "/api/person/abc"
                                  :request-method :delete)
                         :response)]
        (is (= 201 (:status response)))
        (is (received? repository remove-person!))
        (is (received? repository remove-person! ["abc"]))))

    (testing "Marking the build as fixed"
      (let [response (-> (session handler)
                         (request "/api/person/abc"
                                  :request-method :post)
                         :response)]
        (is (= 201 (:status response)))))))

(deftest fixer-test
  (testing "The first person added becomes the fixer"
    (let [repository (mock PersonRepository
                           {:list-persons [person]
                            :add-person!  person})
          fixer      (mock Fixer
                           {:set-fixer! nil
                            :get-fixer  "abc"})
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person"
                                  :request-method :post
                                  :content-type "application/json"
                                  :body (json/write-str new-person))
                         :response)]
      (is (received? fixer set-fixer!))
      (is (received? fixer set-fixer! ["abc"]))))

  (testing "The fixer is marked"
    (let [repository (mock PersonRepository
                           {:list-persons [person next-person]
                            :add-person!  person})
          fixer      (mock Fixer
                           {:set-fixer! nil
                            :get-fixer  "abc"})
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person")
                         :response)]
      (is (= [(assoc person :fixer true) next-person] (read-json response)))))

  (testing "A new fixer is a marked"
    (let [repository (mock PersonRepository
                           {:list-persons [person next-person]})
          fixer      (mock Fixer
                           {:set-fixer! nil
                            :get-fixer  "abc"})
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person/abc"
                                  :request-method :post)
                         :response)]
      (is (received? fixer set-fixer! ["bcd"]))))

  (testing "The first fixer is rotated to again"
    (let [repository (mock PersonRepository
                           {:list-persons [person next-person]})
          fixer      (mock Fixer
                           {:set-fixer! nil
                            :get-fixer  "bcd"})
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person/bcd"
                                  :request-method :post)
                         :response)]
      (is (received? fixer set-fixer! ["abc"])))))
