(ns slackernews.controllers.layout.html
  (:require [hiccup.page :refer [html5]]))

(defn default
  ""
  [content & {:keys [title] :or {title "Slackernews"}}]
  (html5 [:head
          [:title title]]
         [:body content]))
