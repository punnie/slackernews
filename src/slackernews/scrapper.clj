(ns slackernews.scrapper
  (:require [slackernews.db :as db]
            [slackernews.slack :as slack]))

(defn fetch-users []
  (pmap db/insert-user (-> (slack/get-users) :members)))

(defn fetch-channels []
  (pmap db/insert-channel (-> (slack/get-channels) :channels)))

(defn fetch-groups []
  (pmap db/insert-channel (-> (slack/get-groups) :groups)))

(defn fetch-messages [slack-fn id & {:keys [retrieve-count] :or {retrieve-count 100}}]
  (loop [latest nil]
    (let [response      (slack-fn id
                                  :latest latest
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
