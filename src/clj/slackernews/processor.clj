(ns slackernews.processor
  (:require [slackernews.db.core :as db]
            [clojure.tools.logging :as log]))


(defn get-links-from-text
  ""
  [text]
  (let [matcher (re-matcher #"https?://[^>|`´\"\n\r\s]+" text)]
    (loop [match (re-find matcher)
           result []]
      (if-not match
        result
        (recur (re-find matcher)
               (conj result match))))))

(defn extract-links-from-message
  ""
  [team {:keys [text ts] :as message}]
  (when text
    (get-links-from-text text)))

(defn store-link
  ""
  [team message link]
  (let [user-id          (:user message)
        channel-id       (:channel message)
        user-name        (:name (db/get-user-by-id team user-id))
        channel-name     (:name (db/get-channel-by-id team channel-id))
        link-host        (.getHost (new java.net.URI link))
        message-ts       (:ts message)
        message-slack-id (:slack_id message)
        link             {:url     link
                          :channel channel-name
                          :user    user-name
                          :host    link-host}]
    (db/insert-link team message link)))

(defn process-links
  ""
  [team {:keys [ts] :as message}]
  (doseq [link (extract-links-from-message team message)]
    (log/info "Trying to store URL" link "for team" (:id team))
    (store-link team message link)))

(defn store-message
  ""
  [team message]
  (db/insert-message team message))

(defn process-message
  ""
  [team message]
  (condp = (:subtype message)
    "message_changed" (let [message (:message message)]
                        (store-message team message))
    (do
      (process-links team message)
      (store-message team message))))

(defn process-reaction
  ""
  []
  )
