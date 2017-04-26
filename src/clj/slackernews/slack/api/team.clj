(ns slackernews.slack.api.team
  (:require [slackernews.slack.api.core :refer [slack-request]]))

(defn info
  ""
  [connection & params]
  (slack-request connection "team.info" params))

