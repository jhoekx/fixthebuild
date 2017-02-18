(defproject fixthebuild "0.1.0-SNAPSHOT"
  :description "Web application to rotate a build fixer role"
  :url "http://github.com/jhoekx/fixthebuild"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.473"]
                 [integrant "0.2.1"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-jetty-adapter "1.5.1"]
                 [ring/ring-defaults "0.2.3"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.5.2"]
                 [reagent "0.6.0"]
                 [cljs-http "0.1.42"]]
  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-figwheel "0.5.9"]]
  :cljsbuild {:builds {:dev {:source-paths ["src-cljs/"]
                             :figwheel     true
                             :compiler     {:main       fixthebuild.core
                                            :asset-path "js"
                                            :output-to  "target/figwheel/public/js/fixthebuild.js"
                                            :output-dir "target/figwheel/public/js"
                                            :source-map true}}
                       :min {:source-paths ["src-cljs/"]
                             :compiler     {:main          fixthebuild.core
                                            :output-to     "target/min/public/js/fixthebuild.js"
                                            :output-dir    "target/min/public/js"
                                            :optimizations :advanced
                                            :pretty-print  false
                                            :source-map    "target/min/public/js/fixthebuild.js.map"}
                             :jar          true}}}
  :profiles {:dev     {:dependencies   [[org.clojure/data.json "0.2.6"]
                                        [integrant/repl "0.1.0"]
                                        [peridot "0.4.4"]
                                        [com.gearswithingears/shrubbery "0.4.1"]]
                       :source-paths   ["dev"]
                       :resource-paths ["target/figwheel"]
                       :main           user}
             :uberjar {:prep-tasks     ["compile" ["cljsbuild" "once" "min"]]
                       :resource-paths ["target/min"]}}
  :main fixthebuild.main
  :aot [fixthebuild.main])
