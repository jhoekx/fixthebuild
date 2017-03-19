(ns fixthebuild.api-test
  (:use [peridot.core])
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
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

(defn- make-repository [persons]
  (let [repository (->InMemoryPersonRepository (atom {}))]
    (doseq [person persons]
      (add-person! repository person))
    repository))

(deftest person-test
  (testing "Returning a list of persons"
    (let [repository (make-repository [next-person person])
          fixer      (->InMemoryFixer (atom ""))
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person")
                         :response)]
      (testing "gives status 200"
        (is (= 200 (:status response))))
      (testing "orders the persons by uuid"
        (is (= [person next-person] (read-json response))))))

  (testing "Adding a person"
    (let [repository (make-repository [])
          fixer      (->InMemoryFixer (atom ""))
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person"
                                  :request-method :post
                                  :content-type "application/json"
                                  :body (json/write-str new-person))
                         :response)]
      (testing "gives status 200"
        (is (= 200 (:status response))))
      (testing "adds the person to the repository"
        (let [[{:keys [name mail]}] (list-persons repository)]
          (is (= (:name new-person) name))
          (is (= (:mail new-person) mail))))
      (testing "adds a UUID to the person in the repository"
        (is (string? (:uuid (first (list-persons repository))))))
      (testing "returns the person"
        (is (= new-person (select-keys (read-json response) [:name :mail]))))
      (testing "adds a UUID to the person"
        (is (string? (:uuid (read-json response)))))))

  (testing "Removing a person"
    (let [repository (make-repository [person])
          fixer      (->InMemoryFixer (atom ""))
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person/abc"
                                  :request-method :delete)
                         :response)]
      (testing "gives status OK"
        (is (= 201 (:status response))))
      (testing "removes the person from the repository"
        (is [] (list-persons repository)))))

  (testing "Marking the build as fixed"
    (let [repository (make-repository [person])
          fixer      (->InMemoryFixer (atom ""))
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person/abc"
                                  :request-method :post)
                         :response)]
      (testing "gives status OK"
        (is (= 201 (:status response)))))))

(deftest fixer-test
  (testing "The first person added becomes the fixer"
    (let [repository (make-repository [])
          fixer      (->InMemoryFixer (atom ""))
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person"
                                  :request-method :post
                                  :content-type "application/json"
                                  :body (json/write-str new-person))
                         :response)]
      (let [first-person (read-json response)]
        (is (= (:uuid first-person) (get-fixer fixer))))))

  (testing "The fixer is marked in the list"
    (let [repository (make-repository [person next-person])
          fixer      (->InMemoryFixer (atom "abc"))
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person")
                         :response)]
      (is (= [(assoc person :fixer true) next-person] (read-json response)))))

  (testing "A new fixer is a marked"
    (let [repository (make-repository [person next-person])
          fixer      (->InMemoryFixer (atom "abc"))
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person/abc"
                                  :request-method :post)
                         :response)]
      (is (= "bcd" (get-fixer fixer)))))

  (testing "The first fixer is rotated to again"
    (let [repository (make-repository [person next-person])
          fixer      (->InMemoryFixer (atom "bcd"))
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person/bcd"
                                  :request-method :post)
                         :response)]
      (is (= "abc" (get-fixer fixer)))))

  (testing "When removing a person, the next one becomes the fixer"
    (let [repository (make-repository [person next-person])
          fixer      (->InMemoryFixer (atom "abc"))
          handler    (make-handler repository fixer)
          response   (-> (session handler)
                         (request "/api/person/abc"
                                  :request-method :delete)
                         :response)]
      (is (= "bcd" (get-fixer fixer))))))
