(ns slackernews.oauth
  (:require [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]
            [slackernews.config :as config]
            [slackernews.db.team :as tdb]
            [slackernews.db.user :as udb]
            [slackernews.models.team :as teams]
            [slackernews.models.user :as users]
            [slackernews.slack.api.auth :as auth]
            [slackernews.slack.api.oauth :as oauth]))

(defn retrieve-user-details
  ""
  [oauth-response]
  (let [auth-test-response (:user oauth-response)
        user-name          (:user auth-test-response)
        user-id            (:user_id auth-test-response)
        team-id            (:team_id auth-test-response)]
    (udb/upsert-user (users/build-user {:team_id team-id} {:name user-name :id user-id}))
    {:ok true }))

(defn test-auth
  ""
  [oauth-response]
  (let [token              (:access_token oauth-response)
        connection         {:token token}
        auth-test-response (auth/test connection)]
    (when (:ok auth-test-response)
      (merge oauth-response {:user auth-test-response}))))

(defn retrieve-team-details
  ""
  [oauth-response]
  (do
    (tdb/upsert-team (teams/build-team oauth-response))
    oauth-response))

(defn request-access
  ""
  [code]
  (let [response (oauth/access {:client_id config/client-id :client_secret config/client-secret :code code})]
    (when (:ok response)
      response)))

(defn request-authorization
  ""
  [code]
  (let [steps (some-> code
                      (request-access)
                      (retrieve-team-details)
                      (test-auth)
                      (retrieve-user-details))]
    (if (nil? steps)
      {:ok false}
      steps)))
