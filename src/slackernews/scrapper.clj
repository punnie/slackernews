(ns slackernews.scrapper
  (:require [slackernews.db :as db]
            [slackernews.slack :as slack]
            [org.httpkit.client :as http]
            [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan go-loop alt! timeout <! thread close!]]
            [clojure.tools.logging :as log]))

(def user-agent
  {"User-Agent" "facebookexternalhit/1.1"})

(defstate scrapped-channels
  :start (clojure.string/split (env :scrapped-channels) #","))

(defstate uri-blacklist
  :start (seq '(#"talkdesk.slack.com"
                #"talkdesk.atlassian.net"
                #"s3.ethereal.io")))

(defn fetch-users []
  (doall (pmap db/insert-user (-> (slack/get-users) :members))))

(defn fetch-channels []
  (doall (pmap db/insert-channel (-> (slack/get-channels) :channels))))

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

(defn fetch-link-content [link]
  (try
    (log/info "GETting" link)
    (-> link (http/get {:headers user-agent :socket-timeout 10000 :conn-timeout 10000}))
    (catch Exception e nil)))

(defn scrape-link [link]
  (when-let [response (fetch-link-content link)]
    (let [body (-> response :body)
          page (html/html-resource (java.io.StringReader. body))]
      {:meta    (scrape-head-meta-tags page)
       :og      (into {} (scrape-og-meta-tags page))
       :url     link})))

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

(defn make-uri-unsafe
  "Transform HTTPS to HTTP to overcome SNI limitations"
  [uri]
  (clojure.string/replace uri #"^\s*https" "http"))

(defn retrieve-uri
  "Retrieves a promise with a request to the provided uri"
  [uri]
  (let [options {:timeout       10000
                 :max-redirects 25
                 :user-agent    user-agent
                 :insecure?     true
                 :filter        (http/max-body-filter (* 1024 10000))}]
    (http/get uri options)))

(defn process-uri
  ""
  [uri channel]
  (let [{:keys [opts status headers body error] :as resp} @uri]
    (if error
      (log/info (-> opts :url) " error: " error)
      (log/info (-> opts :url) " status: " status))))

(defn not-blacklisted?
  "Checks if uri is in the blacklist"
  [uri]
  (->> uri-blacklist
       (map #(re-find % uri))
       (every? nil?)))

(defn extract-and-filter-uri
  ""
  [text]
  (when-let [uri (try
                   (re-find #"https?://[^>|`´]+" text)
                   (catch java.lang.NullPointerException e nil))]
    (when (not-blacklisted? uri)
      uri)))

(defn process-message
  ""
  [channel message]
  (when-let [uri (extract-and-filter-uri (-> message :text))]
    (-> uri
        (make-uri-unsafe)
        (retrieve-uri)
        (process-uri channel))))

(defn update-all
  "Force update of all slack's resources"
  []
  (fetch-users)
  (fetch-channels)
  (doseq [channel-name scrapped-channels]
    (let [channel-id             (-> (db/get-channel-by-name channel-name) :id)
          last-message-timestamp (-> (db/get-last-message-from-channel channel-id) :ts)
          messages               (lazy-fetch-messages channel-id last-message-timestamp)]
      (dorun (pmap (partial process-message channel-name) messages)))))

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
