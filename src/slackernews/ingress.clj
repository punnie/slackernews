(ns slackernews.ingress
  (:require [slackernews.slack-api.rtm :as rtm]
            [clojure.core.async :as a]
            [clojure.core.async :refer [chan close! go go-loop onto-chan pub sub unsub unsub-all timeout <! >!!]]
            [aleph.http :as http]
            [manifold.stream :as stream]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]))

(def connections (atom {}))

(defn echo-messages
  [channel]
  (go-loop []
    (let [message (<! channel)]
      (log/info message)
      (recur))))

(defn slack-connect
  [connection channel]
  (let [{:keys [ok url team channels groups users bots] :as slack-response} (rtm/start connection)]
    (when ok
      (let [wss-connection @(http/websocket-client url)
            ping-loop       (go-loop [id 0]
                              (<! (timeout 5000))
                              (log/info "Sending ping to connection" (:token connection))
                              (when-not (stream/closed? wss-connection)
                                (do (stream/put! wss-connection
                                                 (json/write-str {:type "ping"
                                                                  :id id
                                                                  :time (System/currentTimeMillis)}))
                                    (recur (inc id)))))]
        (>!! channel team)
        (onto-chan channel channels false)
        (onto-chan channel groups false)
        (onto-chan channel users false)
        (onto-chan channel bots false)
        (stream/consume #(>!! channel (json/read-str % :key-fn keyword)) wss-connection)
        (swap! connections assoc-in [(:token connection)] {:connection wss-connection
                                                           :ping-loop ping-loop})))))

(defn slack-disconnect
  [connection]
  (let [token (:token connection)]
    (.close (-> @connections (get token) :connection))
    (close! (-> @connections (get token) :ping-loop))
    (swap! connections update-in [token] dissoc token)))

(def ^:dynamic *conn*
  {:url "https://slack.com/api/" :token "xoxb-94923848867-dgXkpREpB9jnSCMkaIh0xJOM"})
