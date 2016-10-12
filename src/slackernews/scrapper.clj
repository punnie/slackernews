(ns slackernews.scrapper
  (:require [slackernews.db :as db]
            [slackernews.slack :as slack]
            [aleph.http :as http]
            [aleph.http.client-middleware :as middleware]
            [byte-streams :as bs]
            [net.cgrand.enlive-html :as html]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan go-loop alt! timeout <! thread close!]]
            [clojure.tools.logging :as log]))

(def user-agent "" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/602.1.50 (KHTML, like Gecko) Version/10.0 Safari/602.1.50")

(def pool (http/connection-pool {:middleware nil
                                 :connection-timeout 5000
                                 :request-timeout 5000}))

(defstate scrapped-channels
  :start (clojure.string/split (env :scrapped-channels) #","))

(defstate uri-blacklist
  :start (seq '(#"talkdesk.slack.com"
                #"talkdesk.atlassian.net"
                #"s3.ethereal.io"
                #"talkdeskapp.com")))

(defn fetch-users
  "Fetches all users on slack"
  []
  (doall (pmap db/insert-user (-> (slack/get-users) :members))))

(defn fetch-channels
  "Fetches all channels on slack"
  []
  (doall (pmap db/insert-channel (-> (slack/get-channels) :channels))))

(defn lazy-fetch-messages
  "Creates a lazy sequence with all messages from one channel"
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

(defn scrape-head-meta-tags
  "Scrapes information from regular HTML meta tags"
  [content]
  (let [title       (-> (html/select content [:head :title]) first :content first)
        description (-> (html/select content [:head [:meta (html/attr= :name "description")]]) first :attrs :content)]
    {:title title :description description}))

(defn scrape-og-meta-tags
  "Scrapes information from open graph meta tags"
  [content]
  (for [tag (html/select content [:head [:meta (html/attr-starts :property "og:")]])]
    (let [tag-attrs (-> tag :attrs)
          key       (keyword (clojure.string/replace (:property tag-attrs) (re-pattern "og:") ""))
          value     (:content tag-attrs)]
      {key value})))

(defn retrieve-uri
  "Retrieves a promise with a request to the provided URI"
  [uri]
  (let [headers   {:user-agent user-agent}
        options   {:accept "text/html" :pool pool :throw-exceptions false :keep-alive false}
        arguments (merge {:headers headers} options)]
    {:uri uri :req (http/get uri arguments)}))

(defn process-uri
  "Takes a HTTP request promise and returns information about the REQ"
  [{:keys [uri req]}]
  (let [{:keys [status
                headers
                body
                error
                trace-redirects] :as resp} @req
        url                                (or (-> trace-redirects last) uri)]
    (if (or error
            (not (= (quot status 100) 2))
            (nil? (:content-type headers))
            (not (re-find #"text/html" (:content-type headers))))
      (log/info url " error: " error status (:content-type headers))
      (let [page (html/html-resource (bs/to-reader body))]
        {:meta    (into {} (scrape-head-meta-tags page))
         :og      (into {} (scrape-og-meta-tags page))
         :url     url
         :host    "none"}))))

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

(defn extract-user
  "Extracts a username from a message"
  [message]
  (try
    (if (= (:subtype message) "bot_message")
      (clojure.string/replace (:username message) #"@" "")
      (:name (db/get-user-by-id (:user message))))
    (catch Exception e nil)))

(defn process-message
  ""
  [channel message]
  (do
    (when-let [uri (extract-and-filter-uri (:text message))]
      (let [user (extract-user message)
            ts   (:ts message)]
        (some-> uri
          (retrieve-uri)
          (process-uri)
          (assoc :user user)
          (assoc :channel channel)
          (assoc :ts ts)
          (db/insert-link))))
    (db/insert-message (assoc message :channel (-> channel db/get-channel-by-name :id)))))

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
  "Set periodic interval when a function is called"
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
