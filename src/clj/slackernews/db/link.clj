(ns slackernews.db.link
  (:require [monger.collection :as mc]
            [monger.query :as mq]
            [slackernews.db.core :refer [db]]))

(def collection "" "links")

(defn insert-link
  ""
  [{:keys [ts channel_id team_id user_id] :as link}]
    (mc/update db collection
               {:ts ts :team_id team_id :channel_id channel_id :user_id user_id}
               link
               {:upsert true}))

(defn fetch-links
  ""
  [{:keys [team_id] :as team} & {:keys [page per-page] :or {page 1 per-page 20}}]
  (mq/with-collection db collection
    (mq/find {:team_id team_id})
    (mq/sort (sorted-map :ts -1))
    (mq/paginate :page page :per-page per-page)))
