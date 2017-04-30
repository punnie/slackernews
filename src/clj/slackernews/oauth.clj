(ns slackernews.oauth
  (:require [taoensso.timbre :as log]
            [slackernews.config :as config]
            [slackernews.db.team :as tdb]
            [slackernews.db.user :as udb]
            [slackernews.entities.team :as teams]
            [slackernews.entities.user :as users]
            [slackernews.slack.api.auth :as auth]
            [slackernews.slack.api.oauth :as oauth]
            [slackernews.slack.api.team :as steam]
            [slackernews.slack.api.users :as susers]))

(defn retrieve-user-details
  ""
  [{:keys [ok oauth-response auth-test-response team] :as state}]
  (log/info state)
  (if ok
    (let [token              (:access_token oauth-response)
          connection         {:token token}
          slack-user-info-response (susers/info connection {:user (:user_id auth-test-response)})]
      (if (:ok slack-user-info-response)
        (let [slack-user (:user slack-user-info-response)
              user (users/slack->local team slack-user)]
          (udb/upsert-user user)
          (merge state {:user user}))))
    state))

(defn retrieve-team-details
  ""
  [{:keys [ok oauth-response auth-test-response] :as state}]
  (log/info state)
  (if ok
    (let [token              (:access_token oauth-response)
          connection         {:token token}
          slack-team-info-response (steam/info connection)]
      (if (:ok slack-team-info-response)
        (let [slack-team (:team slack-team-info-response)
              team (teams/slack->local (merge oauth-response slack-team))]
          (tdb/upsert-team team)
          (merge state {:team team}))))
    state))

(defn test-auth
  ""
  [{:keys [ok oauth-response] :as state}]
  (log/info state)
  (if ok
    (let [token              (:access_token oauth-response)
          connection         {:token token}
          auth-test-response (auth/test connection)]
      (merge state {:ok (:ok auth-test-response) :error (:error auth-test-response) :auth-test-response auth-test-response}))
    state))

(defn request-access
  ""
  [code]
  (let [response (oauth/access {:client_id config/client-id :client_secret config/client-secret :code code})]
    {:ok (:ok response) :error (:error response) :oauth-response response}))

(defn request-authorization
  ""
  [code]
  (-> code
      (request-access)
      (test-auth)
      (retrieve-team-details)
      (retrieve-user-details)))
