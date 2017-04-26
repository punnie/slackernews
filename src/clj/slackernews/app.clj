(ns slackernews.app
  (:require [aleph.http :as http]
            [aleph.netty :as netty]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found resources]]
            [hiccup.core :as h]
            [hiccup.page :as hp]
            [ring.logger :refer [wrap-with-logger]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.util.response :refer [redirect response]]
            [slackernews.config :as config]
            [slackernews.db.user :as udb]
            [slackernews.oauth :as oauth]))

(defn front-page
  ""
  [{:keys [session params] :as request}]
  (hp/html5 [:head
             [:title "Slackernews"]]
            [:body
             [:p
              (str "Hello " (-> request :headers :team))]]))

(defn oauth-authorize
  ""
  [{:keys [params session] :as request}]
  (let [error (:error params)
        code  (:code params)
        state (:state params)]
    (if (nil? error)
      (let [oauth-response (oauth/request-authorization code)]
        (if (:ok oauth-response)
          (let [session (assoc session :user_id (str (:user_id oauth-response)))]
            (-> (redirect "/")
                (assoc :session session)))
          (str "Error!")))
      (str "Error: " error))))

(defn not-found-page
  ""
  []
  (h/html [:p "Page not found!"]))

(defroutes all-routes
  (GET "/" [] front-page)
  (GET "/oauth/authorize" [] oauth-authorize)
  (resources "/")
  (not-found (not-found-page)))

(defn wrap-team-subdomain
  ""
  [handler]
  (fn [request]
    (let [team-name (-> (:host (:headers request))
                        (clojure.string/split #"\.")
                        first)]
      (handler (assoc-in request [:headers :team] team-name)))))

(defn handler
  ""
  []
  (-> #'all-routes
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
