(ns slackernews.slack-rt
  (:require [aleph.http :as http]
            [manifold.stream :as s]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [slackernews.slack :as slack]))

(defn echo-message [message]
  (log/info (json/read-str message :key-fn keyword)))

(defn websocket-conect []
  (let [url  (-> (slack/rtm-start) :url)
        conn @(http/websocket-connection url)]
    (s/consume echo-message conn)))
