(ns slackernews.db
  (:require [mount.core :refer [defstate]]
            [rethinkdb.query :as r]
            [rethinkdb.core :as rc]
            [environ.core :refer [env]]))

(defn parse-rethinkdb-uri
  "Parses an URI in the format rethinkdb://:auth@host:port/database
  to a map to be passed on to the rethinkdb.query/connect function"
  [uri]
  (let [uri (new java.net.URI uri)
        host (.getHost uri)
        port (if (= (.getPort uri) -1) 28015 (.getPort uri))
        db (clojure.string/join (rest (.getPath uri)))]
    {:host host :port port :db db}))

(defstate conn
  :start (apply r/connect (apply concat (parse-rethinkdb-uri (env :database-uri))))
  :stop  (rc/close conn))

(defn get-all-users
  "Fetches all users from the database"
  []
  (-> (r/table "users")
      (r/run conn)))

(defn get-user-by-id
  "Fetches a user given its id"
  [user-id]
  (-> (r/table "users")
      (r/get user-id)
      (r/run conn)))

(defn get-user-by-name
  "Fetches a user given its name"
  [user-name]
  (-> (r/table "users")
      (r/filter (r/fn [row]
                  (r/eq user-name (r/get-field row :name))))
      (r/run conn)))

(defn insert-user
  "Inserts a user into the database"
  [user]
  (-> (r/table "users")
      (r/insert user {:conflict :update :durability :hard})
      (r/run conn)))

(defn get-channel-by-id
  "Fetches a channel by its id"
  [channel-id]
  (-> (r/table "channels")
      (r/filter (r/fn [row]
                  (r/eq channel-id (r/get-field row :id))))
      (r/run conn)
      first))

(defn get-channel-by-name
  "Fetches a channel by its name"
  [channel-name]
  (-> (r/table "channels")
      (r/filter (r/fn [row]
                  (r/eq channel-name (r/get-field row :name))))
      (r/run conn)
      first))

(defn insert-channel
  "Inserts a channel into the database"
  [channel]
  (-> (r/table "channels")
      (r/insert channel {:conflict :update :durability :hard})
      (r/run conn)))

(defn insert-message
  "Inserts a message into the database"
  [message]
  (let [message (-> message (assoc :ts (read-string (-> message :ts))))]
    (-> (r/table "messages")
        (r/insert message {:conflict :update :durability :hard})
        (r/run conn))))

(defn get-last-message-from-channel
  "Fetches the last message of the channel given by its id"
  [channel-id]
  (-> (r/table "messages")
      (r/order-by {:index (r/desc :ts)})
      (r/filter (r/fn [row]
                  (r/eq channel-id (r/get-field row :channel))))
      (r/limit 1)
      (r/run conn)
      first))

(defn get-links
  "Fetches all links detected by slack within messages"
  [& {:keys [page] :or {page 0}}]
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

(defn get-links-from-channel
  "Fetches all links in a given channel detected by slack"
  [channel-id]
  (-> (r/table "messages")
      (r/order-by {:index (r/desc :ts)})
      (r/filter (r/fn [row]
                  (r/and
                   (r/eq channel-id (r/get-field row :channel))
                   (r/gt (r/count (r/get-field row :attachments)) 0))))
      (r/run conn)))
