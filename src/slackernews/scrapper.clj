(ns slackernews.scrapper
  (:require [slackernews.db :as db]
            [slackernews.slack :as slack]
            [clj-http.client :as http]
            [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan go-loop alt! timeout <! thread close!]]
            [clojure.tools.logging :as log]))

(defstate scrapped-channels
  :start (clojure.string/split (env :scrapped-channels) #","))

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
  (log/info "Channels to scrap:" scrapped-channels)
  (doseq [channel-name scrapped-channels]
    (let [channel-id             (-> (db/get-channel-by-name channel-name) :id)
          last-message-timestamp (-> (db/get-last-message-from-channel channel-id) :ts)]
      (log/info "Updating channel" channel-name "...")
      (fetch-channel-messages channel-id :oldest last-message-timestamp)))
  (log/info "Update complete!"))

(defn update-all []
  (log/info "Synchrinising with slack...")
  (log/info "Updating users...")
  (fetch-users)
  (log/info "Updating channels...")
  (fetch-channels)
  (log/info "Updating groups...")
  (fetch-groups)
  (log/info "Updating messages...")
  (update-messages))

(defn scrape-link [link]
  (let [response (http/get link)
        body (-> response :body)
        page (html/html-resource (java.io.StringReader. body))]
    {:title (-> (html/select page [:title]) first :content first)
     :link link}))

(defn set-interval
  [f time-in-ms]
  (let [stop (chan)]
    (go-loop []
      (alt!
        (timeout time-in-ms) (do (<! (thread (f)))
                                 (recur))
        stop :stop))
    stop))

(defstate scrapper-job
  :start (set-interval update-all 300000)
  :stop (close! scrapper-job))
