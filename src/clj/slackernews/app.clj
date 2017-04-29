(ns slackernews.app
  (:require [aleph.http :as http]
            [aleph.netty :as netty]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found resources]]
            [hiccup.core :as h]
            [mount.core :refer [defstate]]
            [ring.logger :refer [wrap-with-logger]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.util.response :refer [redirect response]]
            [slackernews.config :as config]
            [slackernews.controllers.links :as links]
            [slackernews.controllers.oauth :as oauth]
            [slackernews.middleware.team-subdomain :refer [wrap-team-subdomain]]
            [taoensso.timbre :as log]))

(defroutes app-routes
  (GET       "/"                [] links/display)
  (GET       "/oauth/authorize" [] oauth/authorize)
  (resources "/")
  (not-found "Not found!"))

(defn app-handler
  ""
  []
  (-> #'app-routes
      wrap-team-subdomain
      (wrap-session {:store (cookie-store {:key config/cookie-secret})
                     :cookie-attrs {:max-age 3600}})
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

(defstate http-server
  :start (start-server {:handler (app-handler) :port config/http-port})
  :stop (stop-server http-server))
