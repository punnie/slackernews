(ns slackernews.entities.team
  (:require [clojure.string :as string]))

(defn get-id
  ""
  [team]
  (:team_id team))

(defn get-allowed-channels
  ""
  [team]
  (:allowed_channels team))

(defn get-blacklist
  ""
  [team]
  (or (:blacklist team) []))

(defn slack->local
  ""
  [slack-team-data]
  (let [access-token (:access_token slack-team-data)
        domain       (:domain slack-team-data)
        scopes       (string/split (:scope slack-team-data) #",")
        team-id      (:team_id slack-team-data)
        team-name    (:team_name slack-team-data)
        user-id      (:user_id slack-team-data)]
    {:access_token access-token
     :scopes scopes
     :team_id team-id
     :name team-name
     :domain domain
     :user_id user-id}))
