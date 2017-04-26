(ns slackernews.models.team
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [slackernews.db.team :as tdb]))

(defn get-id
  ""
  [team]
  (:team_id team))

(defn build-team
  ""
  [slack-team-data]
  (let [access-token (:access_token slack-team-data)
        scopes       (string/split (:scope slack-team-data) #",")
        team-id      (:team_id slack-team-data)
        team-name    (:team_name slack-team-data)
        user-id      (:user_id slack-team-data)]
    {:access_token access-token
     :scopes scopes
     :team_id team-id
     :team_name team-name
     :user_id user-id}))
