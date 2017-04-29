(ns slackernews.core
  (:require [mount.core :as mount]
            [slackernews.app :as app]
            [slackernews.config]
            [slackernews.instrumentation]
            [taoensso.timbre :as log])
  (:gen-class))

(log/swap-config! assoc-in [:level] :info)

(defn stop-app []
  (doseq [component (-> (mount/stop)
                        :stopped)]
    (log/info component "stopped")))

(defn start-app [& args]
  (doseq [component (-> args
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main
  ""
  [& args]
  (start-app args))
