(ns slackernews.engine
  (:require [slackernews.ingress :as ingress]
            [slackernews.db.core :as db]
            [slackernews.reactor :as reactor]
            [clojure.core.async :refer [go-loop
                                        <!]]
            [clojure.tools.logging :as log]))

(defn team-loop
  ""
  [{:keys [team out-stream in-stream] :as context}]
  (go-loop []
    (when-let [{:keys [type] :as slack-event} (<! in-stream)]
      (if-let [fun (ns-resolve (find-ns 'slackernews.reactor) (symbol type))]
        (apply fun context slack-event)
        (log/info "Unimplemented event:" slack-event))
      (recur))))

(defn team-connect
  ""
  [{:keys [connection] :as team}]
  (when-let [{:keys [slack-team
                     slack-channels
                     slack-groups
                     slack-users
                     slack-bots
                     in-stream
                     out-stream]} (ingress/slack-connect connection)]
    (do (db/update-team connection team)
        (log/debug "User count" (count slack-users))
        (log/debug "Bot count" (count slack-bots))
        (doall (pmap (partial db/update-user client) slack-users))
        (doall (pmap (partial db/update-user client) slack-bots))
        (doall (pmap (partial db/update-channel client) slack-channels))
        (doall (pmap (partial db/update-channel client) slack-groups)))
    (team-loop {:team (assoc client :slack team) :out-stream out-stream :in-stream in-stream})
    out-stream))
