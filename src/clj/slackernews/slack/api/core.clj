(ns slackernews.slack.api.core
  (:require [clj-http.client :as http]
            [clojure.core.async :as a]
            [byte-streams :as bs]
            [clojure.data.json :as json]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]))

(def slack-api-url "Slack's API URL" "https://slack.com/api/")

(defn slack-request
  ""
  [connection endpoint & [params]]
  (log/debug params)
  (let [slack-api-base-url (or (:url connection) slack-api-url)
        slack-token        (:token connection)
        slack-url          (str slack-api-base-url endpoint)
        slack-params       (merge {:token slack-token} (into {} params))]
    (-> (http/get slack-url {:query-params slack-params})
        :body
        (json/read-str :key-fn keyword))))
