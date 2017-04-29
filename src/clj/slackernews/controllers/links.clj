(ns slackernews.controllers.links
  (:require [slackernews.controllers.layout.html :as layout]))

(defn display
  ""
  [{:keys [session params] :as request}]
  (layout/default [:p (str "Hello " (str (or (:team request) "nil")))]))
