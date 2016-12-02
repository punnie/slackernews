(defproject slackernews "0.1.0-SNAPSHOT"
  :description "Link aggregator for slack on the style of HackerNews or lobste.rs"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [aleph "0.4.2-alpha8"]
                 [com.apa512/rethinkdb "0.15.26"]
                 [clj-http "2.3.0"]
                 [compojure "1.5.1"]
                 [enlive "1.1.6"]
                 [environ "1.1.0"]
                 [hiccup "1.0.5"]
                 [http-kit "2.1.18"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [mount "0.1.10"]
                 [org.clojure/core.async "0.2.385"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.5"]
                 [ring "1.5.0"]
                 [ring-logger "0.7.6"]]
  :main ^:skip-aot slackernews.core
  :target-path "target/%s"
  :profiles {:uberjar      {:aot :all}
             :dev          [:profiles/dev :project/dev]
             :test         [:profiles/test :project/test]
             :project/dev  {:plugins [[lein-environ "1.1.0"]]}
             :project/test {}})
