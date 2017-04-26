(ns slackernews.db.user
  (:require [slackernews.db.core :refer [db]]
            [monger.collection :as mc]
            [monger.conversion :refer [from-db-object]]
            [monger.operators :refer [$set]]
            [clojure.tools.logging :as log])
  (:import org.bson.types.ObjectId))

(def collection "" "users")

(defn get-user-by-id
  "Gets a user by its ID"
  [team user-id]
  (let [team-id (:team_id team)]
    (from-db-object (mc/find-one db collection {:team_id team-id :user_id user-id}) true)))

(defn upsert-user
  "Updates or inserts a user given certain fields"
  [{:keys [team_id user_id] :as user}]
  (log/info user)
  (mc/find-and-modify db collection
                      {:team_id team_id :user_id user_id}
                      {$set user}
                      {:upsert true :return-new true}))

(defn update-multiple
  "Updates multiple users at once"
  [team users]
  (let [team-id    (:id team)
        users      (->> users
                        (map #(dissoc % :team_id))
                        (map #(assoc % :team team-id)))]
    (dorun
     (pmap #(mc/update db collection {:id (:id %) :team (:team %)} % {:upsert true}) users))))
