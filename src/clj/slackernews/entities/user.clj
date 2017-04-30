(ns slackernews.entities.user)

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
  [{:keys [team_id]} {:keys [id name real_name]}]
  {:real_name real_name :name name :user_id id :team_id team_id})
