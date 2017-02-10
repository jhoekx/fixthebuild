(ns fixthebuild.api-test
  (:use [peridot.core])
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [fixthebuild.api :refer :all]))

(deftest person-test
  (let [handler (make-handler)]
    (testing "Return a list of persons"
      (let [response (-> (session handler)
                         (request "/api/person")
                         :response)]
        (is (= 200 (:status response)))
        (is (= [] (json/read-str (:body response))))))))
