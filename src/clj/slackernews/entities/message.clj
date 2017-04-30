(ns slackernews.entities.message)

(defn build-message
  ""
  [team channel message]
  (let [channel-id (:channel_id channel)
        reactions  (:reactions message)
        team-id    (:team_id team)
        text       (:text message)
        ts         (:ts message)
        user-id    (or (:user message) (:bot_id message))]
    {:channel_id channel-id
     :reactions reactions
     :team_id team-id
     :text text
     :ts ts
     :user_id user-id}))

(defn get-urls-from-text
  ""
  [text]
  (let [matcher (re-matcher #"https?://[^>|`´\"\n\r\s\\]+" text)]
    (loop [match (re-find matcher)
           result []]
      (if-not match
        result
        (recur (re-find matcher)
               (conj result match))))))

(defn extract-urls-from-message
  ""
  [{:keys [text]}]
  (when text
    (get-urls-from-text text)))

(defn get-user-id
  ""
  [message]
  (:user message))

(defn get-ts
  ""
  [message]
  (:ts message))

(defn get-team-id
  ""
  [message]
  (:team_id message))
