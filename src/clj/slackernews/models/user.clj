(ns slackernews.models.user)

(defn get-name
  ""
  [user]
  (:user_name user))

(defn get-id
  ""
  [user]
  (:user_id user))

(defn build-user
  ""
  [{:keys [team_id] :as team} {:keys [id name] :as user-data}]
  {:user_name name :user_id id :team_id team_id})
