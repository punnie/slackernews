(ns slackernews.slack.api.rtm
  (:require [slackernews.slack.api.core :refer [slack-request]]))

(defn start
  ""
  [connection & params]
  (slack-request connection "rtm.start" params))
