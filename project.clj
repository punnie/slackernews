(defproject slackernews "0.1.0-SNAPSHOT"
  :description "Link aggregator for slack on the style of HackerNews or lobste.rs"
  :url "https://slackrnews.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [aleph "0.4.3"]
                 [clj-http "3.5.0"]
                 [compojure "1.5.1"]
                 [com.novemberain/monger "3.1.0"]
                 [enlive "1.1.6"]
                 [environ "1.1.0"]
                 [hiccup "1.0.5"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [mount "0.1.10"]
                 [org.clojure/core.async "0.2.385"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.5"]
                 [reagent "0.6.0"]
                 [ring "1.5.0"]
                 [ring-logger "0.7.6"]]
  :plugins [[lein-cljsbuild "1.1.4"]
            [jonase/eastwood "0.2.3"]]
  :main ^:skip-aot slackernews.core
  :target-path "target/%s"
  :resource-paths ["resources" "target/cljsbuild"]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]

  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]

  :figwheel
  {:http-server-root "public"
   :nrepl-port 7002
   :css-dirs ["resources/public/css"]
   :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles {:uberjar      {:aot :all}
             :dev          [:profiles/dev :project/dev]
             :test         [:profiles/test :project/test]
             :project/dev  {:dependencies [[binaryage/devtools "0.8.3"]
                                           [com.cemerick/piggieback "0.2.2-SNAPSHOT"]]
                            :plugins [[lein-environ "1.1.0"]
                                      [lein-figwheel "0.5.8"]]
                            :cljsbuild
                            {:builds
                             {:app
                              {:source-paths ["src/cljs" "env/dev/cljs"]
                               :test-paths ["test/cljs"]
                               :compiler
                               {:main "slackernews-frontend.app"
                                :asset-path "/js/out"
                                :output-to "target/cljsbuild/public/js/app.js"
                                :output-dir "target/cljsbuild/public/js/out"
                                :source-map true
                                :optimizations :none
                                :pretty-print true}}}}}
             :project/test {}})
