(ns slackernews.models.link-test
  (:require [slackernews.models.link :refer :all]
            [clojure.test :refer :all]))

(deftest host-extraction
  (testing "When given a link"
    (is (= (get-link-host "https://www.twitter.com/punnie") "www.twitter.com"))))
