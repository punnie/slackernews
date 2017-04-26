(ns slackernews.slack.api.auth
  (:require [slackernews.slack.api.core :refer [slack-request]]))

(defn test
  ""
  [connection & params]
  (slack-request connection "auth.test" params))
