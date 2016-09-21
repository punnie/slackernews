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
  (doall (pmap db/insert-user (-> (slack/get-users) :members))))

(defn fetch-channels []
  (doall (pmap db/insert-channel (-> (slack/get-channels) :channels))))

(defn fetch-groups []
  (doall (pmap db/insert-channel (-> (slack/get-groups) :groups))))

(defn fetch-messages [slack-fn id & {:keys [retrieve-count oldest]
                                     :or {retrieve-count 100}}]
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
      (fetch-channel-messages channel-id :oldest last-message-timestamp))))

(defn update-all []
  (log/info "Updating with slack...")
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
  :start (set-interval update-all (read-string (env :scrapping-interval)))
  :stop (close! scrapper-job))

(defn scrape-head-meta-tags [content]
  (let [title (-> (html/select content [:head :title]) first :content first)
        description (-> (html/select content [:head [:meta (html/attr= :name "description")]]) first :attrs :content)]
    {:title title :description description}))

(defn scrape-og-meta-tags [content]
  (for [tag (html/select content [:head [:meta (html/attr-starts :property "og:")]])]
    (let [tag-attrs (-> tag :attrs)
          key (keyword (clojure.string/replace (:property tag-attrs) (re-pattern "og:") ""))
          value (:content tag-attrs)]
      {key value})))





(defn fetch-link-content-type [link]
  (try
    (log/info "HEADing" link)
    (-> link (http/head {:socket-timeout 1000 :conn-timeout 1000}) :headers (get "Content-Type"))
    (catch Exception e nil)))

(defn fetch-link-content [link]
  (try
    (log/info "GETting" link)
    (-> link (http/get {:socket-timeout 1000 :conn-timeout 1000}))
    (catch Exception e nil)))

(defn scrape-link [link]
  (when-let [response (fetch-link-content link)]
    (let [body (-> response :body)
          page (html/html-resource (java.io.StringReader. body))]
      {:meta    (scrape-head-meta-tags page)
       :og      (into {} (scrape-og-meta-tags page))
       :url     link})))



(defn extract-link-from-message [message]
  (try
    (re-find #"https?://[^>|]+" (:text message))
    (catch java.lang.NullPointerException e nil)))

(defn extract-user-from-message [message]
  (try
    (if (= (:subtype message) "bot_message")
      (clojure.string/replace (:username message) #"@" "")
      (:name (db/get-user-by-id (:user message))))
    (catch Exception e nil)))

(defn extract-channel-from-message [message]
  (:name (db/get-channel-by-id (:channel message))))

(defn extract-ts-from-message [message]
  (-> message :ts))

(defn filter-messages [message]
  (when-let [link (extract-link-from-message message)]
    (let [content-type (fetch-link-content-type link)]
      (try
        (clojure.string/starts-with? content-type "text/html")
        (catch java.lang.NullPointerException e nil)))))

(defn process-messages [message]
  (log/info "***********")
  (log/info message)
  (log/info "***********")
  (let [link    (scrape-link (extract-link-from-message message))
        ts      (extract-ts-from-message message)
        channel (extract-channel-from-message message)
        user    (extract-user-from-message message)]
    {:link      link
     :ts        ts
     :channel   channel
     :user      user}))

(defn pipeline-messages [messages]
  (->> messages
       (filter filter-messages)
       (pmap process-messages)))

