(ns slackernews.processor.scraper
  (:require [clojure.java.io :as io]
            [http.async.client :as http]
            [http.async.client.request :as request]
            [pantomime.mime :as mime]
            [taoensso.timbre :as log])
  (:import [java.net UnknownHostException
                     NoRouteToHostException]
           [javax.net.ssl SSLProtocolException]))

(def user-agent "" (str "slackernews/"
                        (System/getProperty "slackernews.version")
                        " (by github.com/punnie)"))

(defn request-uri
  ""
  [{:keys [url] :as link-info} & {:keys [max-size] :or {max-size (* 64 1024)}}]
  (with-open [client (http/create-client :follow-redirects true
                                         :user-agent user-agent
                                         :compression-enabled true
                                         :connection-timeout 10000
                                         :request-timeout 10000
                                         :idle-in-pool-timeout 10000)]
    (let [size    (atom 0)
          request (request/prepare-request :get url)
          resp    (request/execute-request client request
                                           :part (fn [resp part]
                                                   (request/body-collect resp part)
                                                   (reset! size (+ @size (.size part)))
                                                   (if (> @size max-size)
                                                     [part :abort]
                                                     [part :continue])))]
      (assoc-in link-info [:response] (http/await resp)))))

(defn get-mime-type
  ""
  [{:keys [response] :as link-info}]
  (let [body (.toByteArray @(:body response))]
    (assoc-in link-info [:mime-type] (mime/mime-type-of body))))

(defn get-link-information
  ""
  [{:keys [url] :as link-info}]
  (-> link-info
      (request-uri)
      (get-mime-type)
      (get-in [:response :url])))
