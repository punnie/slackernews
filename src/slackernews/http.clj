(ns slackernews.http
  (:require [org.httpkit.server :as http-kit]
            [clojure.tools.logging :as log]))

(defn start [{:keys [handler host port] :as opts}]
  (try
    (log/info "Starting HTTP server at port :" port)
    (http-kit/run-server handler (dissoc opts :handler :init))
    (catch Throwable t
      (log/error "Error launching HTTP server!")
      (throw t))))

(defn stop [http-server]
  (http-server :timeout 100))
