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

(defn get-links
  ""
  []
  {})

(defn update-team
  "Updates a team's name, domain and email_domain"
  [connection slack-data & {:keys [return-changes]}]
  (let [token        (:token connection)
        name         (:name slack-data)
        domain       (:domain slack-data)
        email_domain (:email_domain slack-data)
        update-data  {:name name :domain domain :email_domain email_domain}]
    (-> (r/table "teams")
        (r/get-all [token] {:index "connection_token"})
        (r/update update-data {:return_changes return-changes})
        (r/run conn))))

(defn get-team
  ""
  [connection]
  (let [token (:token connection)]
    (-> (r/table "teams")
        (r/get-all [token] {:index "connection_token"})
        (r/run conn)
        first)))

(defn update-users
  ""
  []
  {})
