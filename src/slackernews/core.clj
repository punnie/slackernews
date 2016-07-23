(ns slackernews.core
  (:require [mount.core :as mount]
            [slackernews.http :as http]
            [slackernews.handler :as handler])
  (:gen-class))

(mount/defstate http-server
  :start (http/start {:handler (handler/app) :port 3000})
  :stop (http/stop http-server))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
