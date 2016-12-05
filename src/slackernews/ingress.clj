(ns slackernews.ingress
  (:require [slackernews.slack-api.rtm :as rtm]
            [clojure.core.async :refer [chan
                                        close!
                                        go
                                        go-loop
                                        onto-chan
                                        pipeline
                                        pub
                                        sub
                                        unsub
                                        unsub-all
                                        timeout
                                        <!
                                        >!
                                        >!!]]
            [aleph.http :as http]
            [manifold.stream :as stream]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]))

(def xf-parallelism 1)
(def xf-json-encode (map json/write-str))
(def xf-json-decode (map #(json/read-str % :key-fn keyword)))

(defn ping-loop
  ""
  [out in]
  (go-loop [id 0]
    (<! (timeout 5000))
    (if-not (>! out {:type "ping"
                     :id id
                     :time (System/currentTimeMillis)})
      (>! in {:time (System/currentTimeMillis)
              :type "error"
              :message "disconnected"})
      (recur (inc id)))))

(defn slack-connect
  ""
  [connection]
  (let [{:keys [ok url team channels groups users bots] :as slack-response} (rtm/start connection)]
    (when ok
      (let [wss-connection    @(http/websocket-client url)
            in-raw-channel     (chan)
            out-raw-channel    (chan)
            in-stream-channel  (chan)
            out-stream-channel (chan)
            _                  (stream/connect wss-connection in-raw-channel)
            _                  (stream/connect out-raw-channel wss-connection)
            _                  (pipeline xf-parallelism in-stream-channel xf-json-decode in-raw-channel)
            _                  (pipeline xf-parallelism out-raw-channel xf-json-encode out-stream-channel)
            ping-loop          (ping-loop out-stream-channel in-stream-channel)]
        {:team team
         :channels channels
         :groups groups
         :users users
         :bots bots
         :in-stream in-stream-channel
         :out-stream out-stream-channel}))))
