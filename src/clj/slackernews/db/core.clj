(ns slackernews.db.core
  (:require [mount.core :refer [defstate]]
            [environ.core :refer [env]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer [$set]]
            [monger.query :as mq]
            [monger.conversion :refer [from-db-object]]
            [clojure.tools.logging :as log]))


(defstate db*
  :start (-> env :database-url mg/connect-via-uri)
  :stop (-> db* :conn mg/disconnect))

(defstate db
  :start (-> db* :db))

(defn update-team
  "Updates a team's name, domain and email_domain"
  [connection slack-data & {:keys [return-new-document] :or {return-new-document false}}]
  (let [token       (:token connection)
        collection  "teams"
        update-data (-> slack-data
                        (select-keys [:name :domain :email_domain]))
        result      (mc/find-and-modify db collection
                                        {:connection.token token}
                                        {$set update-data}
                                        {:return-new return-new-document})]
    (if return-new-document
      (dissoc result :_id)
      result)))

(defn get-team
  "Gets a team by connection token"
  [{:keys [token]}]
  (let [collection "teams"
        result (mc/find-one db collection {:connection.token token})]
    (-> result (from-db-object true) (dissoc :_id))))

(defn get-user-by-id
  "Gets a user by its ID"
  [team user-id]
  (let [team-id    (:id team)
        collection "users"]
    (log/info "Fetching user" user-id "from team" (:id team))
    (when-let [result (mc/find-one db collection {:team team-id :id user-id})]
      (-> result (from-db-object true) (dissoc :_id)))))

(defn update-user
  "Updates a user"
  [id update-data]
  (let [collection "users"]
    (mc/find-and-modify db collection {:id id} {$set update-data} {})))

(defn update-users
  "Updates multiple users at once"
  [team users]
  (let [team-id    (:id team)
        collection "users"
        users      (->> users
                        (map #(dissoc % :team_id))
                        (map #(assoc % :team team-id)))]
    (dorun
     (pmap #(mc/update db collection {:id (:id %) :team (:team %)} % {:upsert true}) users))))

(defn get-channel-by-id
  "Gets a channel by its ID"
  [team channel-id]
  (let [team-id    (:id team)
        collection "channels"]
    (log/info "Fetching channel" channel-id "from team" (:id team))
    (when-let [result (mc/find-one db collection {:team team-id :id channel-id})]
      (-> result (from-db-object true) (dissoc :_id)))))

(defn get-channel-by-name
  "Gets a channel by its NAME"
  [team channel-name]
  (let [team-id    (:id team)
        collection "channels"]
    (log/info "Fetching channel" channel-name "from team" (:id team))
    (when-let [result (mc/find-one db collection {:team team-id :name channel-name})]
      (-> result (from-db-object true) (dissoc :_id)))))

(defn update-channels
  "Updates multiple channels at once"
  [team channels]
  (let [team-id    (:id team)
        collection "channels"
        channels   (->> channels
                        (map #(dissoc % :team_id))
                        (map #(assoc % :team team-id)))]
    (dorun
     (pmap #(mc/update db collection {:id (:id %) :team (:team %)} % {:upsert true}) channels))))

(defn insert-message
  ""
  [team message]
  (let [collection "messages"
        ts         (:ts message)
        team-id    (:id team)
        sane-ts    (clojure.string/replace ts #"\." "")
        message-id (str team-id "-" sane-ts)
        slack-id   (str "p" sane-ts)
        message    (merge message {:team team-id :id message-id :slack_id slack-id})]
    (mc/update db collection {:id message-id} message {:upsert true})))

(defn insert-link
  ""
  [team message link]
  (let [collection "links"
        ts         (:ts message)
        team-id    (:id team)
        sane-ts    (clojure.string/replace ts #"\." "")
        link-id    (str team-id "-" sane-ts)
        slack-id   (str "l" sane-ts)
        link       (merge link {:ts ts :team team-id :id link-id :slack_id slack-id})]
    (mc/update db collection {:id link-id} link {:upsert true})))

(defn get-links
  "Gets links"
  [& {:keys [page] :or {page 0}}]
  (let [per-page   25
        skip       (* page per-page)
        collection "links"
        results    (mq/with-collection db collection
                     (mq/find {})
                     (mq/sort (sorted-map :ts -1))
                     (mq/skip skip)
                     (mq/limit per-page))]
    results))

; (defn get-links
;   "TODO: fix for multi-tenancy"
;   [& {:keys [page] :or {page 0}}]
;   (let [per-page 25
;         skip (* page per-page)]
;     (-> (r/table "links")
;         (r/order-by {:index (r/desc :ts)})
;         (r/skip skip)
;         (r/limit per-page)
;         (r/run conn))))
