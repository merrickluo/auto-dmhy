(defproject dmhy "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [lein-sub "0.3.0"]
                 [org.clojars.scsibug/feedparser-clj "0.5.0"]]
  :sub ["modules/feedparser"]
  :main ^:skip-aot dmhy.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
