(ns slackernews.controllers.links
  (:require [slackernews.controllers.layout.html :as layout]
            [slackernews.db.link :as ldb]))

(defn- display-links
  ""
  [team]
  (let [links (ldb/fetch-links team)]
    [:ul (for [link links]
           [:li (str link)])]))

(defn- display-add-to-slack
  ""
  []
  [:p (str "Add to slack!")])

(defn display
  ""
  [{:keys [session params team] :as request}]
  (layout/default
   (if team
     (display-links team)
     (display-add-to-slack))))
