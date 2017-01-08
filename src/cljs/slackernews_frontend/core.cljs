(ns slackernews-frontend.core
  (:require [reagent.core :as r :refer [atom]]))

(def app-db (atom nil))

(defn app-container
  ""
  []
  [:ul
   (for [number ["One" "Two" "Three"]]
     [:li number])])

(defn mount-components
  ""
  []
  (r/render-component [app-container]
                      (.getElementById js/document "app")))

(defn init!
  ""
  []
  (mount-components))
