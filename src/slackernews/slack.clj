(ns slackernews.slack
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [clojure.data.json :as json]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]))

(defstate slack-token
  :start (env :slack-token))

(defn rtm-start []
  (-> @(http/get (str "https://slack.com/api/rtm.start?token=" slack-token))
      :body
      bs/to-string
      (json/read-str :key-fn keyword)))

(defn get-users []
  (-> @(http/get (str "https://slack.com/api/users.list?token=" slack-token))
      :body
      bs/to-string
      (json/read-str :key-fn keyword)))

(defn get-channels []
  (-> @(http/get (str "https://slack.com/api/channels.list?token=" slack-token))
      :body
      bs/to-string
      (json/read-str :key-fn keyword)))

(defn get-channel-messages [channel-id & {:keys [latest oldest inclusive retrieve-count unreads]
                                          :or {latest (quot (System/currentTimeMillis) 1000)
                                               inclusive 0
                                               retrieve-count 100
                                               unreads 0}}]
  (let [url (str "https://slack.com/api/channels.history?"
                 "token=" slack-token
                 "&channel=" channel-id
                 "&latest=" latest
                 "&oldest=" oldest
                 "&inclusive=" inclusive
                 "&count=" retrieve-count
                 "&unreads=" unreads)]
    (log/info "GETting" url)
    (-> @(http/get url)
        :body
        bs/to-string
        (json/read-str :key-fn keyword))))
