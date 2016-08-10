(ns slackernews.db
  (:require [mount.core :refer [defstate]]
            [rethinkdb.query :as r]
            [rethinkdb.core :as rc]))

(defstate conn
  :start (r/connect :host "192.168.99.100" :port 28015 :db "slackernews")
  :stop  (rc/close conn))

(defn get-all-users []
  (-> (r/table "users")
      (r/run conn)))

(defn get-user-by-id [user-id]
  (-> (r/table "users")
      (r/get user-id)
      (r/run conn)))

(defn get-user-by-name [user-name]
  (-> (r/table "users")
      (r/filter (r/fn [row]
                  (r/eq user-name (r/get-field row :name))))
      (r/run conn)))

(defn insert-user [user]
  (-> (r/table "users")
      (r/insert user {:conflict :update :durability :hard})
      (r/run conn)))

(defn get-channel-by-id [channel-id]
  (-> (r/table "channels")
      (r/filter (r/fn [row]
                  (r/eq channel-id (r/get-field row :id))))
      (r/run conn)
      first))

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

(defn get-last-message-from-channel [channel-id]
  (-> (r/table "messages")
      (r/order-by {:index (r/desc :ts)})
      (r/filter (r/fn [row]
                  (r/eq channel-id (r/get-field row :channel))))
      (r/limit 1)
      (r/run conn)
      first))

(defn get-links [& {:keys [page] :or {page 0}}]
  (let [per-page 25
        skip (* page per-page)]
    (-> (r/table "messages")
        (r/order-by {:index (r/desc :ts)})
        (r/filter (r/fn [row]
                    (r/and
                     (r/gt (r/count (r/get-field row :attachments)) 0)
                     (r/match (r/get-field row :text) "<http.*>")
                     (r/eq "message" (r/get-field row :type)))))
        (r/skip skip)
        (r/limit per-page)
        (r/run conn))))

(defn get-links-from-channel [channel-id]
  (-> (r/table "messages")
      (r/order-by {:index (r/desc :ts)})
      (r/filter (r/fn [row]
                  (r/and
                   (r/eq channel-id (r/get-field row :channel))
                   (r/gt (r/count (r/get-field row :attachments)) 0))))
      (r/run conn)))
