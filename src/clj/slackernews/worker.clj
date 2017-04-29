(ns slackernews.worker
  (:require [slackernews.processor.core :as processor]
            [slackernews.slack.api.channels :as channels]
            [slackernews.slack.api.users :as users]
            [slackernews.db.channel :as cdb]))

(defn fetch-channel-message-history
  ""
  [team channel]
  (let [connection {:token (:access_token team)}
        channel-id (:channel_id channel)]
    (doseq [message (channels/history->lazy-seq connection channel-id)]
      (processor/process-message team channel message))))

(defn fetch-team-message-history
  "Fetches the latest message history for a team"
  [team]
  (let [allowed-channels (or (:allowed_channels team) [])]
    (doseq [channel-id allowed-channels]
      (let [channel    (cdb/get-channel-by-id team channel-id)]
        (fetch-channel-message-history team channel)))))

(defn fetch-team-channels
  ""
  [team]
  (let [connection       {:token (:access_token team)}
        channel-response (channels/list connection)]
    (if (:ok channel-response)
      (doseq [slack-channel (:channels channel-response)]
        (processor/process-channel team slack-channel)))))

(defn fetch-team-users
  ""
  [team]
  (let [connection    {:token (:access_token team)}
        user-response (users/list connection)]
    (if (:ok user-response)
      (doseq [slack-user (:members user-response)]
        (processor/process-user team slack-user)))))
