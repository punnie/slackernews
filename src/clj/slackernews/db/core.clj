(ns slackernews.db.core
  (:require [environ.core :refer [env]]
            [monger.core :as mg]
            [mount.core :refer [defstate]]))

(defstate db*
  :start (-> env :database-url mg/connect-via-uri)
  :stop (-> db* :conn mg/disconnect))

(defstate db
  :start (-> db* :db))
