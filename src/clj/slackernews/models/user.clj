(ns slackernews.models.user)

(defn get-name
  ""
  [user]
  (:name user))

(defn get-id
  ""
  [user]
  (:user_id user))

(defn build-user
  ""
  [{:keys [team_id] :as team} {:keys [id name real_name] :as user-data}]
  {:real_name real_name :name name :user_id id :team_id team_id})
