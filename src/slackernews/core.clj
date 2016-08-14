(ns slackernews.core
  (:require [mount.core :as mount]
            [slackernews.http-server :as http]
            [slackernews.nrepl-server :as nrepl]
            [slackernews.handler :as handler]
            [slackernews.scrapper :as scrapper]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(mount/defstate ^{:on-reload :noop}
  http-server
  :start (http/start {:handler (handler/app) :port 3000})
  :stop (http/stop http-server))

(mount/defstate ^{:on-reload :noop}
  nrepl-server
  :start (nrepl/start {:port 7070})
  :stop (nrepl/stop nrepl-server))

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
