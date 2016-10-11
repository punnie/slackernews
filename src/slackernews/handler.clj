(ns slackernews.handler
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
            [slackernews.db :as db]
            [clojure.tools.logging :as log]))

(defn layout [options & body]
  (hp/html5 [:head
             (hp/include-css "/css/slackernews.css")
             [:title (:title options "Slackernews")]]
            [:body
             [:div#wrapper
              body]]))

(defn render-pages [page]
  [:p.pages
   (when (> page 0)
     [:span.prev-page [:a {:href (str "/?page=" (dec page))} "<< Previous page"]])
   [:span.next-page [:a {:href (str "/?page=" (inc page))} "Next page >>"]]])

(defn render-landing-page [req]
  (let [page  (-> req :params (:page "0") read-string)
        links (db/get-links :page page)]
    (layout {:title "Slackernews"}
            [:ul.link-list (for [link links]
                             (let [url     (-> link :url)
                                   title   (or (-> link :meta :title) url)
                                   host    (-> link :host)
                                   user    (-> link :user)
                                   channel (-> link :channel)]
                               [:li
                                [:p.link-title
                                 [:a {:href url} (h/h title)]
                                 [:span.host host]]
                                [:p.link-description
                                 (str "via " user " at #" channel)]]))]
            (render-pages page))))

(defn not-found-page []
  (h/html [:p "Page not found!"]))

(defroutes all-routes
  (GET "/" [] render-landing-page)
  (resources "/")
  (not-found (not-found-page)))

(defn app []
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
