(defproject dmhy "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.3.0"]
                 [org.clojure/core.async "0.3.441"]
                 [lein-sub "0.3.0"]
                 [compojure "1.5.2"]
                 [org.clojure/data.json "0.2.6"]
                 [com.novemberain/monger "3.1.0"]
                 [org.clojars.scsibug/feedparser-clj "0.5.0"]]
  :plugins [[lein-ring "0.11.0"]]
  :sub ["modules/feedparser"]
  :main ^:skip-aot dmhy.core
  :target-path "target/%s"
  :ring {:handler dmhy.core/app}
  :profiles {:uberjar {:aot :all}})
