(ns slackernews.db.link
  (:require [monger.collection :as mc]
            [slackernews.db.core :refer [db]]))

(def collection "" "links")

(defn insert-link
  ""
  [{:keys [ts channel_id team_id user_id] :as link}]
    (mc/update db collection
               {:ts ts :team_id team_id :channel_id channel_id :user_id user_id}
               link
               {:upsert true}))
