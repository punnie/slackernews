(ns slackernews.db.core
  (:require [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [monger.collection :as mc]
            [monger.conversion :refer [from-db-object]]
            [monger.core :as mg]
            [monger.operators :refer [$set]]
            [monger.query :as mq]
            [mount.core :refer [defstate]]))


(defstate db*
  :start (-> env :database-url mg/connect-via-uri)
  :stop (-> db* :conn mg/disconnect))

(defstate db
  :start (-> db* :db))
