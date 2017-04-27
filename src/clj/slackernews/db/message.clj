(ns slackernews.db.message
  (:require [monger.collection :as mc]
            [slackernews.db.core :refer [db]]))

(def collection "" "messages")

(defn insert-message
  ""
  [{:keys [ts channel_id team_id user_id] :as message}]
  (mc/update db collection
             {:ts ts :channel_id channel_id :team_id team_id :user_id user_id}
             message
             {:upsert true}))
