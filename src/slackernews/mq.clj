(ns slackernews.mq
  (:require [clojure.core.async :refer [chan close! mult pub]]
            [mount.core :refer [defstate]]))

(defstate publisher
  :start (chan)
  :stop (close! publisher))

(defstate debug-multiplier
  :start (mult publisher))

(defstate publication
  :start (pub publisher :type)
