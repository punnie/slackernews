(ns slackernews.slack-api.channels
  (:require [slackernews.slack-api.core :refer [slack-request]]))

(defn list
  ""
  [connection & params]
  (slack-request connection "channels.list" params))

(defn history
  ""
  [connection & params]
  (slack-request connection "channels.history" params))

(defn history->lazy-seq
  "Creates a lazy sequence with all messages from one channel"
  ([connection channel-id]
   (history->lazy-seq connection channel-id nil nil))
  ([connection channel-id oldest-ts]
   (history->lazy-seq connection channel-id oldest-ts nil))
  ([connection channel-id oldest-ts latest-ts]
   (lazy-seq
    (let [response (history connection {:channel channel-id :latest latest-ts :oldest oldest-ts})
          messages (-> response :messages)]
      (when (not-empty messages)
        (concat messages
                (history->lazy-seq connection channel-id oldest-ts (-> messages last :ts))))))))
