(ns slackernews.controllers.oauth
  (:require [ring.util.response :refer [redirect]]
            [slackernews.oauth :as oauth]))

(defn authorize
  ""
  [{:keys [params session] :as request}]
  (let [error (:error params)
        code  (:code params)
        state (:state params)]
    (if (nil? error)
      (let [oauth-response (oauth/request-authorization code)]
        (if (:ok oauth-response)
          (let [session (assoc session :user_id (str (:user_id oauth-response)))]
            (-> (redirect "/")
                (assoc :session session)))
          (str "Error!")))
      (str "Error: " error))))
