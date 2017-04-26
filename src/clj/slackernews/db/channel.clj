(ns slackernews.db.channel
  (:require [slackernews.db.core :refer [db]]
            [monger.collection :as mc]
            [monger.conversion :refer [from-db-object]]
            [clojure.tools.logging :as log]))

(def collection "" "channels")

(defn get-channel-by-id
  "Gets a channel by its ID"
  [team channel-id]
  (let [team-id (:id team)]
    (when-let [result (mc/find-one db collection {:team team-id :channel_id channel-id})]
      (-> result (from-db-object true) (dissoc :_id)))))

(defn get-channel-by-name
  "Gets a channel by its name"
  [team channel-name]
  (let [team-id (:team_id team)]
    (log/info "Fetching channel" channel-name "from team" (:id team))
    (when-let [result (mc/find-one db collection {:team team-id :name channel-name})]
      (-> result (from-db-object true) (dissoc :_id)))))

(defn update-multiple-channels
  "Updates multiple channels at once"
  [team channels]
  (let [team-id    (:team_id team)
        channels   (->> channels
                        (map #(assoc % :team_id team-id)))]
    (dorun
     (pmap #(mc/update db collection
                       {:channel_id (:channel_id %) :team_id (:team_id %)}
                       %
                       {:upsert true}) channels))))

(defn upsert-channel
  ""
  [{:keys [team_id channel_id] :as channel}]
  (mc/update db collection
             {:team_id team_id :channel_id channel_id}
             channel
             {:upsert true}))
