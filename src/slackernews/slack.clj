(ns slackernews.slack
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]))

(defstate slack-token
  :start (env :slack-token))

(defn http-sync-get
  "Performs a synchronous get using http-kit"
  [uri]
  (let [{:keys [error] :as resp} @(http/get uri)]
    (if error
      (log/error "Failed, exception:" error)
      resp)))

(defn get-users []
  (-> (str "https://slack.com/api/users.list?token=" slack-token)
      http-sync-get
      :body
      (json/read-str :key-fn keyword)))

(defn get-channels []
  (-> (str "https://slack.com/api/channels.list?token=" slack-token)
      http-sync-get
      :body
      (json/read-str :key-fn keyword)))

(defn get-groups []
  (-> (str "https://slack.com/api/groups.list?token=" slack-token)
      http-sync-get
      :body
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
    (-> url
        http-sync-get
        :body
        (json/read-str :key-fn keyword))))
