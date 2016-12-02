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

(defn messages-seq
  "Creates a lazy sequence with all messages from one channel"
  ([channel-id]
   (messages-seq channel-id nil nil))
  ([channel-id first-ts]
   (messages-seq channel-id first-ts nil))
  ([channel-id first-ts last-ts]
   (lazy-seq
    (let [response (history {:channel channel-id :latest last-ts :oldest first-ts})
          messages (-> response :messages)]
      (when (not-empty messages)
        (concat messages
                (messages-seq channel-id first-ts (-> messages last :ts))))))))
