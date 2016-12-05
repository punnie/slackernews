(ns slackernews.engine
  (:require [slackernews.ingress :as ingress]
            [slackernews.db.core :as db]
            [slackernews.reactor :as reactor]
            [clojure.core.async :refer [go-loop
                                        <!]]
            [clojure.tools.logging :as log]))

; {:id "060A6EEC-4C66-4FFC-AF55-F48D9BC0A2E6"
;  :connection {:url "https://slack.com/api/" :token "xoxb-94923848867-dgXkpREpB9jnSCMkaIh0xJOM"}}
; (condp = type
;         "hello"           (let [timestamp (System/currentTimeMillis)]
;                             (log/info "Connected to slack at" timestamp))
;         "pong"            (let [timestamp (System/currentTimeMillis)]
;                             (log/info "Last pong received at" timestamp))
;         "user_change"     (let [user          (:user slack-event)
;                                 user-slack-id (:id user)]
;                             (log/info "Updating user information for user" user-slack-id)
;                             (db/update-user user-slack-id user))
;         "presence_change" (let [presence      (:presence slack-event)
;                                 user-slack-id (:user slack-event)]
;                             (log/info "Updating user presence for user" user-slack-id)
;                             (db/update-user-presence user-slack-id presence))
;         "channel_created" (let [channel          (:channel slack-event)
;                                 channel-slack-id (:id channel)]
;                             (log/info "Creating new channel" channel-slack-id)
;                             (db/create-channel channel))
;         (log/info slack-event))

(defn client-loop
  ""
  [{:keys [team out-stream in-stream] :as context}]
  (go-loop []
    (when-let [{:keys [time type subtype] :as slack-event} (<! in-stream)]
      (if-let [fun (ns-resolve (find-ns 'slackernews.reactor) (symbol type))]
        (apply fun context slack-event)
        (log/info "Unimplemented event:" slack-event))
      (recur))))

(defn client-connect
  ""
  [{:keys [connection] :as client}]
  (when-let [{:keys [team
                     channels
                     groups
                     users
                     bots
                     in-stream
                     out-stream]} (ingress/slack-connect connection)]
    (do (db/update-team client team)
        (log/debug "User count" (count users))
        (log/debug "Bot count" (count bots))
        (doall (pmap (partial db/upsert-user client) users))
        (doall (pmap (partial db/upsert-user client) bots))
        (doall (pmap (partial db/upsert-channel client) channels))
        (doall (pmap (partial db/upsert-channel client) groups)))
    (client-loop {:team (assoc client :slack team) :out-stream out-stream :in-stream in-stream})
    out-stream))
