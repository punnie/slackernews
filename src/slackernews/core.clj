(ns slackernews.core
  (:require [mount.core :as mount]
            [slackernews.http :as http]
            [slackernews.handler :as handler]
            [slackernews.scrapper :as scrapper]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [environ.core :refer [env]])
  (:gen-class))

(mount/defstate http-server
  :start (http/start {:handler (handler/app) :port 3000})
  :stop (http/stop http-server))

(defn stop-app []
  (doseq [component (-> (mount/stop)
                        :stopped)]
    (log/info component "stopped")))

(defn start-app [args]
  (doseq [component (-> args
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (start-app args))
