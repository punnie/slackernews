(ns slackernews.config
  (:require [environ.core :refer [env]]
            [mount.core :refer [defstate]]))

(defstate cookie-secret
  :start (:cookie-secret env))

(defstate client-id
  :start (:client-id env))

(defstate client-secret
  :start (:client-secret env))

(defstate riemann-host
  :start (or (:riemann-host env) "localhost"))

(defstate riemann-port
  :start (Integer/parseInt (or (:riemann-port env) "5555")))

(defstate http-port
  :start (Integer/parseInt (or (:port env) "8080")))

(defstate http-domain
  :start (:http-domain "slackrnews.com"))
