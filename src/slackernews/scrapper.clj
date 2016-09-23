(ns slackernews.scrapper
  (:require [slackernews.db :as db]
            [slackernews.slack :as slack]
            [clj-http.client :as http]
            [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan go-loop alt! timeout <! thread close!]]
            [clojure.tools.logging :as log]))

(def ua {"User-Agent" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/602.1.50 (KHTML, like Gecko) Version/10.0 Safari/602.1.50"})

(defstate scrapped-channels
  :start (clojure.string/split (env :scrapped-channels) #","))

(defn fetch-users []
  (doall (pmap db/insert-user (-> (slack/get-users) :members))))

(defn fetch-channels []
  (doall (pmap db/insert-channel (-> (slack/get-channels) :channels))))

(defn fetch-groups []
  (doall (pmap db/insert-channel (-> (slack/get-groups) :groups))))

(defn lazy-fetch-messages
  ([channel-id]
   (lazy-fetch-messages channel-id nil nil))
  ([channel-id first-ts]
   (lazy-fetch-messages channel-id first-ts nil))
  ([channel-id first-ts last-ts]
   (lazy-seq
    (let [response (slack/get-channel-messages channel-id :latest last-ts :oldest first-ts)
          messages (-> response :messages)]
      (when (not-empty messages)
        (concat messages
                (lazy-fetch-messages channel-id first-ts (-> messages last :ts))))))))

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
    (-> link (http/head {:headers ua :socket-timeout 10000 :conn-timeout 10000}) :headers (get "Content-Type"))
    (catch Exception e nil)))

(defn fetch-link-content [link]
  (try
    (log/info "GETting" link)
    (-> link (http/get {:headers ua :socket-timeout 10000 :conn-timeout 10000}))
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

(defn process-messages [channel message]
  (let [url     (extract-link-from-message message)
        link    (scrape-link url)
        ts      (extract-ts-from-message message)
        user    (extract-user-from-message message)]
    {:link      link
     :ts        ts
     :channel   channel
     :user      user}))

(defn update-all []
  (fetch-users)
  (fetch-channels)
  (fetch-groups)
  (doseq [channel-name scrapped-channels]
    (let [channel-id             (-> (db/get-channel-by-name channel-name) :id)
          last-message-timestamp (-> (db/get-last-message-from-channel channel-id) :ts)
          messages               (lazy-fetch-messages channel-id last-message-timestamp)]
      (dorun
       (->> messages (filter filter-messages) (pmap (partial process-messages channel-name)) (pmap db/insert-link))))))

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
