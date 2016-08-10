(ns slackernews.slack
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]))

(def token "changeme")

(defn get-users []
  (-> (str "https://slack.com/api/users.list?token=" token)
      http/get
      :body
      (json/read-str :key-fn keyword)))

(defn get-channels []
  (-> (str "https://slack.com/api/channels.list?token=" token)
      http/get
      :body
      (json/read-str :key-fn keyword)))

(defn get-groups []
  (-> (str "https://slack.com/api/groups.list?token=" token)
      http/get
      :body
      (json/read-str :key-fn keyword)))

(defn get-channel-messages [channel-id & {:keys [latest oldest inclusive retrieve-count unreads]
                                          :or {latest (quot (System/currentTimeMillis) 1000)
                                               oldest 0
                                               inclusive 0
                                               retrieve-count 100
                                               unreads 0}}]
  (-> (str "https://slack.com/api/channels.history?token="
           token
           "&channel="
           channel-id
           "&latest="
           latest
           "&oldest="
           (format "%.6f" oldest)
           "&inclusive="
           inclusive
           "&count="
           retrieve-count
           "&unreads="
           unreads)
      http/get
      :body
      (json/read-str :key-fn keyword)))


(defn get-group-messages [group-id & {:keys [latest oldest inclusive retrieve-count unreads]
                                      :or {latest (quot (System/currentTimeMillis) 1000)
                                           oldest 0
                                           inclusive 0
                                           retrieve-count 100
                                           unreads 0}}]
  (-> (str "https://slack.com/api/groups.history?token="
           token
           "&channel="
           group-id
           "&latest="
           latest
           "&oldest="
           oldest
           "&inclusive="
           inclusive
           "&count="
           retrieve-count
           "&unreads="
           unreads)
      http/get
      :body
      (json/read-str :key-fn keyword)))
