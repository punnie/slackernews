(ns slack-archive.core
  (:require [mount.core :as mount]
            [slack-archive.db :as db]
            [slack-archive.slack :as slack])
  (:gen-class))

(defn fetch-users []
  (pmap db/insert-user (-> (slack/get-users) :members)))

(defn fetch-channels []
  (pmap db/insert-channel (-> (slack/get-channels) :channels)))

(defn fetch-groups []
  (pmap db/insert-channel (-> (slack/get-groups) :groups)))

(defn fetch-channel-messages [channel-id & {:keys [retrieve-count] :or {retrieve-count 100}}]
  (loop [latest nil]
    (let [response      (slack/get-channel-messages channel-id
                                                    :latest latest
                                                    :retrieve-count retrieve-count)
          messages      (-> response :messages)
          has-more      (-> response :has_more)
          message-count (count messages)]
      (when (> message-count 0)
        (pmap #(let [message (-> % (assoc :channel channel-id))]
                  (db/insert-message message)) messages))
      (when (= has-more true)
        (recur (-> messages last :ts))))))

(defn fetch-group-messages [group-id & {:keys [retrieve-count] :or {retrieve-count 100}}]
  (loop [latest nil]
    (let [response      (slack/get-group-messages group-id
                                                  :latest latest
                                                  :retrieve-count retrieve-count)
          messages      (-> response :messages)
          has-more      (-> response :has_more)
          message-count (count messages)]
      (when (> message-count 0)
        (pmap #(let [message (-> % (assoc :channel group-id))]
                 (db/insert-message message)) messages))
      (when (= has-more true)
        (recur (-> messages last :ts))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
