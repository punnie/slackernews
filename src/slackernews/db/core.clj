(ns slackernews.db.core
  (:require [mount.core :refer [defstate]]
            [rethinkdb.query :as r]
            [rethinkdb.core :as rc]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log]
            [slackernews.db.core :as db]))

(defstate conn
  :start (r/connect :host (env :database-host)
                    :port (read-string (env :database-port))
                    :db   (env :database-db))
  :stop  (rc/close conn))

(defn update-team
  "Updates a team's name, domain and email_domain"
  [connection slack-data & {:keys [return-new-document]}]
  (let [token         (:token connection)
        update-data   (-> slack-data
                          (select-keys [:name :domain :email_domain]))
        update-result (-> (r/table "teams")
                          (r/get-all [token] {:index "connection_token"})
                          (r/update update-data {:return_changes "always"})
                          (r/run conn))
        new-document  (-> update-result
                          :changes
                          first
                          :new_val)]
    (if return-new-document
      new-document
      update-result)))

(defn get-team
  ""
  [{:keys [token]}]
  (-> (r/table "teams")
      (r/get-all [token] {:index "connection_token"})
      (r/run conn)
      first))

(defn get-user-by-id
  ""
  [team user-id]
  (try
    (let [team-id (:id team)]
      (log/info "Fetching user" user-id "from team" team-id)
      (-> (r/table "users")
          (r/get-all [[team-id user-id]] {:index "team_id_id"})
          (r/limit 1)
          (r/run conn)
          first))
    (catch Exception e
      (log/error "Caught exception" e "while searching for user. Ignoring."))))

(defn update-user
  ""
  [id update-data]
  (-> (r/table "users")
      (r/get id)
      (r/update update-data)
      (r/run conn)))

(defn update-users
  ""
  [team users]
  (let [team-id (:id team)
        users   (->> users
                     (map #(dissoc % :team_id))
                     (map #(assoc % :team team-id)))]
    (-> (r/table "users")
        (r/insert users {:conflict "update"})
        (r/run conn))))

(defn get-channel-by-id
  ""
  [team channel-id]
  (let [team-id (:id team)]
    (log/info "Fetching channel" channel-id "from team" team-id)
    (-> (r/table "channels")
        (r/get-all [[team-id channel-id]] {:index "team_id_id"})
        (r/limit 1)
        (r/run conn)
        first)))

(defn get-channel-by-name
  ""
  [team channel-name]
  (let [team-id (:id team)]
    (log/info "Fetching channel" channel-name "from team" team-id)
    (-> (r/table "channels")
        (r/get-all [[team-id channel-name]] {:index "team_id_name"})
        (r/limit 1)
        (r/run conn)
        first)))

(defn update-channels
  ""
  [team channels]
  (let [team-id (:id team)
        channels (->> channels
                      (map #(dissoc % :team_id))
                      (map #(assoc % :team team-id)))]
    (-> (r/table "channels")
        (r/insert channels {:conflict "update"})
        (r/run conn))))

(defn insert-message
  ""
  [team message]
  (let [ts         (:ts message)
        team-id    (:id team)
        sane-ts    (clojure.string/replace ts #"\." "")
        message-id (str team-id "-" sane-ts)
        slack-id   (str "p" sane-ts)
        message    (merge message {:team team-id :id message-id :slack_id slack-id})]
    (-> (r/table "messages")
        (r/insert message {:conflict "update"})
        (r/run conn))))

(defn insert-link
  ""
  [team message link]
  (let [ts         (:ts message)
        team-id    (:id team)
        sane-ts    (clojure.string/replace ts #"\." "")
        link-id    (str team-id "-" sane-ts)
        slack-id   (str "l" sane-ts)
        link       (merge link {:ts ts :team team-id :id link-id :slack_id slack-id})]
    (-> (r/table "links")
        (r/insert link {:conflict "update"})
        (r/run conn))))

(defn get-links
  "TODO: fix for multi-tenancy"
  [& {:keys [page] :or {page 0}}]
  (let [per-page 25
        skip (* page per-page)]
    (-> (r/table "links")
        (r/order-by {:index (r/desc :ts)})
        (r/skip skip)
        (r/limit per-page)
        (r/run conn))))
