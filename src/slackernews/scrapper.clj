(ns slackernews.scrapper
  (:require [slackernews.db :as db]
            [slackernews.slack :as slack]
            [clj-http.client :as http]
            [net.cgrand.enlive-html :as html]))

(def scrapped-channels ["meaningful" "list" "of" "channels"])

(defn fetch-users []
  (pmap db/insert-user (-> (slack/get-users) :members)))

(defn fetch-channels []
  (pmap db/insert-channel (-> (slack/get-channels) :channels)))

(defn fetch-groups []
  (pmap db/insert-channel (-> (slack/get-groups) :groups)))

(defn fetch-messages [slack-fn id & {:keys [retrieve-count oldest]
                                     :or {retrieve-count 100 oldest 0}}]
  (loop [latest nil]
    (let [response      (slack-fn id
                                  :latest latest
                                  :oldest oldest
                                  :retrieve-count retrieve-count)
          messages      (-> response :messages)
          has-more      (-> response :has_more)
          message-count (count messages)]
      (when (> message-count 0)
        (pmap #(let [message (-> % (assoc :channel id))]
                 (db/insert-message message)) messages))
      (when (= has-more true)
        (recur (-> messages last :ts))))))

(def fetch-channel-messages
  (partial fetch-messages slack/get-channel-messages))

(def fetch-group-messages
  (partial fetch-messages slack/get-group-messages))

(defn update-messages []
  (for [channel-name scrapped-channels]
    (let [channel-id             (-> (db/get-channel-by-name channel-name) :id)
          last-message-timestamp (-> (db/get-last-message-from-channel channel-id) :ts)]
      (fetch-channel-messages channel-id :oldest last-message-timestamp))))

(defn scrape-link [link]
  (let [response (http/get link)
        body (-> response :body)
        page (html/html-resource (java.io.StringReader. body))]
    {:title (-> (html/select page [:title]) first :content first)
     :link link}))
