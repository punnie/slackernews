(ns slackernews.engine
  (:require [slackernews.ingress :as ingress]
            [slackernews.db.core :as db]
            [slackernews.processor :as processor]
            [slackernews.reactor :as reactor]
            [slackernews.slack-api.channels :as channels]
            [clojure.core.async :refer [go-loop
                                        thread
                                        <!]]
            [clojure.tools.logging :as log]))

(def sync-channels ["tech-corner"])

(defn fetch-channels-history
  ""
  [{:keys [connection] :as team}]
  (doseq [channel-name sync-channels]
    (let [team-id    (:id team)
          channel-id (:id (db/get-channel-by-name team channel-name))]
      (doseq [message (channels/history->lazy-seq connection channel-id)]
        (let [message (-> message
                          (assoc :team team-id)
                          (assoc :channel channel-id))]
          (processor/process-message team message))))))

(defn team-loop
  ""
  [{:keys [team out-stream in-stream] :as context}]
  (go-loop []
    (when-let [{:keys [type] :as slack-event} (<! in-stream)]
      (if-let [fun (ns-resolve (find-ns 'slackernews.reactor) (symbol type))]
        (apply fun [context slack-event])
        (log/info "Unimplemented event:" slack-event))
      (recur))))

(defn team-connect
  ""
  [{:keys [connection]}]
  (when-let [{:keys [team
                     channels
                     groups
                     users
                     bots
                     in-stream
                     out-stream]} (ingress/slack-connect connection)]
    (let [team    (db/update-team connection team :return-new-document true)
          context {:team team :out-stream out-stream :in-stream in-stream}
          _       (db/update-users team users)
          _       (db/update-channels team channels)
          _       (db/update-channels team groups)
          _       (thread (fetch-channels-history team))]
      (team-loop context))))
