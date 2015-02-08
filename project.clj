(defproject pixicljs "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2755"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :node-dependencies [[source-map-support "0.2.8"]]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-npm "0.4.0"]]

  :source-paths ["src" "target/classes"]

  :clean-targets ["out" "out-adv"]

  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {
                                   :main pixicljs.dev
                                   :foreign-libs [{:file "js-lib/pixi.dev.js"
                                                   :provides ["pixilib"]}]
                                   :output-to "out/pixicljs.js"
                                   :output-dir "out"
                                   :optimizations :whitespace
                                   :cache-analysis true
                                   :source-map "out/pixicljs.js.map"}}]})
