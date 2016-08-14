(defproject slackernews "0.1.0-SNAPSHOT"
  :description "Link aggregator for slack on the style of HackerNews or lobste.rs"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.2.0"]
                 [enlive "1.1.6"]
                 [com.apa512/rethinkdb "0.15.26"]
                 [org.clojure/data.json "0.2.6"]
                 [mount "0.1.10"]
                 [http-kit "2.1.18"]
                 [compojure "1.5.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [hiccup "1.0.5"]]
  :main ^:skip-aot slackernews.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
