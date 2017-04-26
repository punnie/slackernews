(ns slackernews.db.team
  (:require [clojure.tools.logging :as log]
            [monger.collection :as mc]
            [monger.conversion :refer [from-db-object]]
            [monger.operators :refer [$set]]
            [slackernews.db.core :refer [db]]))

(def collection "" "teams")

(defn upsert-team
  "Updates or inserts a team given certain fields of info about it"
  [{:keys [team_id] :as team}]
  (mc/find-and-modify db collection
                      {:team_id team_id}
                      {$set team}
                      {:return-new true :upsert true}))

(defn create-team-channels
  "Creates a list of empty allowed channels for a team"
  [team]
  (let [team-id (:team_id team)
        create-result (mc/find-and-modify db collection
                                          {:team_id team-id}
                                          {$set {:allowed_channels []}}
                                          {:return-new true})]
    (:allowed_channels create-result)))

(defn get-team-by-name
  "Gets a team by name"
  [name]
  (let [result (mc/find-one db collection {:team_name name})]
    (-> result (from-db-object true) (dissoc :_id))))
