(ns slackernews.handler
  (:require [compojure.handler :refer [site]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            [hiccup.core :as h]))

(defn show-landing-page [req]
  (h/html [:p "Hello world!"]))

(defn login-page [req]
  (h/html [:p "Want to login?"]))

(defn not-found-page []
  (h/html [:p "Page not found!"]))

(defroutes all-routes
  (GET "/" [] show-landing-page)
  (GET "/login" [] login-page)
  (not-found (not-found-page)))

(defn app []
  (site #'all-routes))
