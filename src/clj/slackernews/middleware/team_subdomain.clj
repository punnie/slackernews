(ns slackernews.middleware.team-subdomain
  (:require [clojure.string :refer [split]]
            [slackernews.config :as config]
            [slackernews.db.team :refer [get-team-by-domain]]
            [taoensso.timbre :as log]))

; TODO: improve subdomain detection (works for now)

(defn wrap-team-subdomain
  ""
  [handler]
  (fn [request]
    (let [host-header (:host (:headers request))
          team-domain (-> (:host (:headers request))
                          (split #"\.")
                          first)
          team        (get-team-by-domain team-domain)]
      (handler (assoc-in request [:team] team)))))
