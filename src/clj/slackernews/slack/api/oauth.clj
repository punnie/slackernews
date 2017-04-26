(ns slackernews.slack.api.oauth
  (:require [slackernews.slack.api.core :refer [slack-request]]))

(defn access
  ""
  [& params]
  (slack-request {} "oauth.access" params))

