(ns slackernews.entities.link)

(defn get-link-host
  ""
  [link]
  (.getHost (new java.net.URI link)))

(defn slack->local
  ""
  [team message {:keys [ts user_id user_name channel_id channel_name team_id title url] :as link-info}]
  (let [link-host (get-link-host url)]
    {:ts ts
     :team_id team_id
     :channel_id channel_id
     :user_id user_id
     :channel channel_name
     :user user_name
     :url url
     :title title
     :host link-host}))
