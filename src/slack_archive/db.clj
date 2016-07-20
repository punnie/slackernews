(ns slack-archive.db
  (:require [mount.core :refer [defstate]]
            [rethinkdb.query :as r]
            [rethinkdb.core :as rc]))

(defstate conn
  :start (r/connect :host "192.168.99.100" :port 28015 :db "slack_archive")
  :stop  (rc/close conn))

(defn insert-user [user]
  (-> (r/table "users")
      (r/insert user {:conflict :update :durability :hard})
      (r/run conn)))

(defn get-channel-by-name [name]
  (-> (r/table "channels")
      (r/filter (r/fn [row]
                  (r/eq name (r/get-field row :name))))
      (r/run conn)
      first))

(defn insert-channel [channel]
  (-> (r/table "channels")
      (r/insert channel {:conflict :update :durability :hard})
      (r/run conn)))

(defn insert-message [message]
  (let [message (-> message (assoc :ts (read-string (-> message :ts))))]
    (-> (r/table "messages")
        (r/insert message {:conflict :update :durability :hard})
        (r/run conn))))
