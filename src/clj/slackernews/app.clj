(ns slackernews.app
  (:require [aleph.http :as http]
            [aleph.netty :as netty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.logger :refer [wrap-with-logger]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found resources]]
            [hiccup.core :as h]
            [hiccup.page :as hp]
            [slackernews.db.core :as db]
            [clojure.tools.logging :as log]))

(defn layout [options & body]
  (hp/html5 [:head
             (hp/include-css "/css/slackernews.css")
             [:title (:title options "Slackernews")]]
            [:body
             [:div#app]
             (hp/include-js "/js/app.js")]))

(defn not-found-page []
  (h/html [:p "Page not found!"]))

(defroutes all-routes
  (GET "/" [] layout)
  (resources "/")
  (not-found (not-found-page)))

(defn handler []
  (-> #'all-routes
      wrap-session
      wrap-keyword-params
      wrap-params
      wrap-with-logger))

(defn start-server
  ""
  [{:keys [handler host port] :as opts}]
  (try
    (log/info "Starting HTTP server on port" port)
    (let [server (http/start-server handler {:port port})]
      (future (netty/wait-for-close server))
      server)
    (catch Throwable t
      (log/error t (str "server failed to start on port " port))
      (throw t))))

(defn stop-server
  ""
  [server]
  (.close server))
