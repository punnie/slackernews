(ns slackernews.processor.core
(:require [taoensso.timbre :as log]
          [slackernews.entities.channel :as channels]
          [slackernews.entities.link :as links]
          [slackernews.entities.message :as messages]
          [slackernews.entities.team :as teams]
          [slackernews.entities.user :as users]
          [slackernews.db.channel :as cdb]
          [slackernews.db.link :as ldb]
          [slackernews.db.message :as mdb]
          [slackernews.db.user :as udb]))

(defn store-message
  ""
  [team channel message]
  (mdb/insert-message (messages/slack->local team channel message)))

(defn store-link
  ""
  [team channel message link-info]
  (ldb/insert-link (links/slack->local team message link-info)))

(defn process-links
  ""
  [team channel message]
  (doseq [url (messages/extract-urls-from-message message)]
    (let [channel-id   (channels/get-id channel)
          channel-name (channels/get-name channel)
          team-id      (teams/get-id team)
          ts           (messages/get-ts message)
          user         (udb/get-user-by-id team (messages/get-user-id message))
          user-id      (users/get-id user)
          user-name    (users/get-name user)
          link-info    {:url url
                        :user_id user-id
                        :user_name user-name
                        :channel_id channel-id
                        :channel_name channel-name
                        :ts ts
                        :team_id team-id}]
      (if (nil? user-name) (log/info message))
      (store-link team channel message link-info))))

(defn process-message
  ""
  [team channel message]
  (do
    (store-message team channel message)
    (process-links team channel message)))

(defn process-channel
  ""
  [team slack-channel]
  (cdb/upsert-channel (channels/slack->local team slack-channel)))

(defn process-user
  ""
  [team slack-user]
  (log/info team slack-user)
  (udb/upsert-user (users/slack->local team slack-user)))
