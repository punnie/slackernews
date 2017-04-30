(ns slackernews.processor.blacklist
  (:require [slackernews.entities.team :as teams]
            [taoensso.timbre :as log]))

(defn blacklisted?
  ""
  [url blacklist]
  (not (nil? (->> blacklist
                  (map #(some? (re-find (re-pattern %) url)))
                  (some true?)))))

(defn filter-link
  ""
  [{:keys [url team] :as link-info}]
  (let [blacklist (teams/get-blacklist team)]
    (assoc link-info :blacklisted (blacklisted? url blacklist))))
