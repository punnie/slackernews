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
             [:div#ribbon
              [:a {:href "https://github.com/talkdesk/slackernews"}
               [:img {:style "position: absolute; top: 0; right: 0; border: 0;"
                      :src "https://camo.githubusercontent.com/38ef81f8aca64bb9a64448d0d70f1308ef5341ab/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f6461726b626c75655f3132313632312e706e67"
                      :alt "Fork me on GitHub"
                      :data-canonical-src "https://s3.amazonaws.com/github/ribbons/forkme_right_darkblue_121621.png"}]]]
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
    (layout {:title "Slackernews - Talkdesk"}
            [:ul.link-list (for [link links]
                             (let [url     (-> link :link :url)
                                   title   (-> link :link :meta :title)
                                   host    (-> (new java.net.URI url) .getHost)
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
      wrap-params))
