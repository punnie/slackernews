(ns slackernews.processor.blacklist-test
  (:require [slackernews.processor.blacklist :refer :all]
            [clojure.test :refer :all]))

(deftest blacklisted-test
  (testing "When a link is filtered in the blacklist"
    (let [simple-blacklist ["s3.ethereal.io" "talkdesk.slack.com"]]
      (is (blacklisted? "http://s3.ethereal.io/Swmd.png" simple-blacklist))
      (is (not (blacklisted? "https://github.com/rhiever/tpot" simple-blacklist))))))
