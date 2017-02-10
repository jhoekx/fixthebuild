(defproject fixthebuild "0.1.0-SNAPSHOT"
  :description "Web application to rotate a build fixer role"
  :url "http://github.com/jhoekx/fixthebuild"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [integrant "0.2.1"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-jetty-adapter "1.5.1"]
                 [ring/ring-defaults "0.2.3"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.5.2"]]
  :profiles {:dev {:dependencies [[org.clojure/data.json "0.2.6"]
                                  [integrant/repl "0.1.0"]
                                  [peridot "0.4.4"]]
                   :source-paths ["dev"]
                   :main         user}}
  :main fixthebuild.main
  :aot [fixthebuild.main])
