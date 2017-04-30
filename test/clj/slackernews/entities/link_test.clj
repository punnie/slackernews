(ns slackernews.entities.link-test
  (:require [slackernews.entities.link :refer :all]
            [clojure.test :refer :all]))

(deftest host-extraction
  (testing "When given a link"
    (is (= (get-link-host "https://www.twitter.com/punnie") "www.twitter.com"))))
