(ns slackernews.instrumentation
  (:require [taoensso.timbre :as log]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]
            [riemann.client :as r]
            [slackernews.config :as config]))

(defstate c
  :start (r/tcp-client {:host config/riemann-host :port config/riemann-port})
  :stop (r/close! c))
