(ns slackernews.slack.api.users
  (:require [slackernews.slack.api.core :refer [slack-request]]))

(defn list
  ""
  [connection & params]
  (slack-request connection "users.list" params))

(defn info
  ""
  [connection & params]
  (slack-request connection "users.info" params))
