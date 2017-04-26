(ns slackernews.config
  (:require [environ.core :refer [env]]
            [mount.core :refer [defstate]]))

(defstate cookie-secret
  :start (-> env :cookie-secret))

(defstate client-id
  :start (-> env :client-id))

(defstate client-secret
  :start (-> env :client-secret))


