(ns slackernews.processor.core
  (:require [slackernews.db.channel :as cdb]
            [slackernews.db.link :as ldb]
            [slackernews.db.message :as mdb]
            [slackernews.db.user :as udb]
            [slackernews.entities.channel :as channels]
            [slackernews.entities.link :as links]
            [slackernews.entities.message :as messages]
            [slackernews.entities.team :as teams]
            [slackernews.entities.user :as users]
            [slackernews.processor.blacklist :as blacklist]
            [slackernews.processor.scraper :as scraper]
            [taoensso.timbre :as log]))

(defn store-message
  ""
  [team channel message]
  (mdb/insert-message (messages/slack->local team channel message)))

(defn store-link
  ""
  [link-info]
  (log/info link-info))

(defn process-links-from-message
  ""
  [team channel message]
  (doseq [url (messages/extract-urls-from-message message)]
    (let [user         (udb/get-user-by-id team (messages/get-user-id message))
          link-info    {:url url
                        :user user
                        :channel channel
                        :message message
                        :team team}]
      (-> link-info
          (blacklist/filter-link)
          (scraper/get-link-information)
          (store-link)))))

(defn process-message
  ""
  [team channel message]
  (do
    (store-message team channel message)
    (process-links-from-message team channel message)))

(defn process-channel
  ""
  [team slack-channel]
  (cdb/upsert-channel (channels/slack->local team slack-channel)))

(defn process-user
  ""
  [team slack-user]
  (log/info team slack-user)
  (udb/upsert-user (users/slack->local team slack-user)))
