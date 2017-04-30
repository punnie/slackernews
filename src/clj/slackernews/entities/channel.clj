(ns slackernews.entities.channel)

(defn get-name
  ""
  [channel]
  (:name channel))

(defn get-id
  ""
  [channel]
  (:channel_id channel))

(defn slack->local
  ""
  [team {:keys [id name_normalized creator purpose topic]}]
  (let [team_id (:team_id team)
        purpose (:value purpose)
        topic   (:value topic)]
    {:team_id team_id
     :name name_normalized
     :creator creator
     :purpose purpose
     :topic topic
     :channel_id id}))
