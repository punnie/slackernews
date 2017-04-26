(ns slackernews.models.message-test
  (:require [slackernews.models.message :refer :all]
            [clojure.test :refer :all]))

(def single-url "" "Thanks, go to http://google.com for more details!")
(def multi-url "" "Thanks, go to http://google.com for more details! Or check http://twitter.com")
(def enclosed-text "" "Check out <https://slack.com> for interesting stuff.")

(deftest url-extraction
  (testing "When there's a single url in the text"
    (is (= (get-urls-from-text single-url) ["http://google.com"])))
  (testing "When there are multiple urls in the text"
    (is (= (get-urls-from-text multi-url) ["http://google.com" "http://twitter.com"])))
  (testing "When slack sends links enclosed in <>"
    (is (= (get-urls-from-text enclosed-text) ["https://slack.com"]))))
