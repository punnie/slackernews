(ns slackernews.processor.scraper
  (:require [clojure.string :as string]
            [http.async.client :as http]
            [http.async.client.request :as request]
            [net.cgrand.enlive-html :as html]
            [pantomime.mime :as mime]
            [taoensso.timbre :as log])
  (:import (java.io StringReader)))

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
  (let [body (.toByteArray (http/body response))]
    (assoc-in link-info [:mime-type] (mime/mime-type-of body))))

(defn get-meta-tags
  ""
  [content]
  (let [title       (-> (html/select content [:head :title])
                        first
                        :content
                        first)
        description (-> (html/select content [:head [:meta (html/attr= :name "description")]])
                        first
                        :attrs
                        :content)
        og          (for [tag (html/select content [:head [:meta (html/attr-starts :property "og:")]])]
                      (let [tag-attrs (-> tag :attrs)
                            key       (keyword (clojure.string/replace (:property tag-attrs) (re-pattern "og:") ""))
                            value     (:content tag-attrs)]
                        {key value}))]
    {:meta {:title title :description description}
     :og   (into {} og)}))

(defn get-meta-information
  ""
  [{:keys [response] :as link-info}]
  (if-let [meta-info (case (:mime-type link-info)
                       ("text/html" "application/xhtml+xml") (-> (http/body response)
                                                                 (.toString)
                                                                 (StringReader.)
                                                                 (html/html-resource)
                                                                 (get-meta-tags))
                       nil)]
    (assoc-in link-info [:meta] meta-info)
    link-info))

(defn get-link-information
  ""
  [{:keys [url] :as link-info}]
  (log/info url)
  (-> link-info
      (request-uri)
      (get-mime-type)
      (get-meta-information)
      :meta))
