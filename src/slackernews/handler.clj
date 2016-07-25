(ns slackernews.handler
  (:require [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.session :refer [wrap-session]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found resources]]
            [hiccup.core :as h]
            [hiccup.page :as hp]
            [slackernews.db :as db]))

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
  (let [page  (read-string (-> req :params (:page 0)))
        links (filter #(contains? % :user) (db/get-links :page page))]
    (layout {:title "Slackernews - Talkdesk"}
            [:ul.link-list (for [link links]
                             (let [url     (-> link :attachments first :from_url)
                                   title   (-> link :attachments first (:fallback "Untitled"))
                                   host    (-> (new java.net.URI url) .getHost)
                                   user    (-> (db/get-user-by-id (-> link :user)) :name)
                                   channel (-> (db/get-channel-by-id (-> link :channel)) :name)]
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
      wrap-params))
