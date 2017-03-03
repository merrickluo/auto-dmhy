(ns dmhy.core
  (:gen-class)
  (:require [feedparser-clj.core :as parser]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.core.async :as async :refer [<! timeout chan go]]))

(def db
  (let [conn (mg/connect)]
    (mg/get-db conn "dmhy")))

(map println (take 1 (:entries (parser/parse-feed "http://share.dmhy.org/topics/rss/rss.xml"))))

(defn settings
  "change settings"
  [req]
  (doseq [[path value] (map identity req)]
    (mc/update db "settings" {:path path} {:path path :value value} {:upsert true}))
  "ok")

(defn add-feed
  "add dmhy rss feed"
  [req]
  (prn req)
  (let [url (get req "url")]
    (mc/update db "feeds" {:url url} {:url url} {:upsert true}))
  "ok")

(mc/find-maps db "settings" {:path "ariarpc"})
(defn body2json
  "parse request body to json"
  [body]
  (json/read-str (slurp body)))

(defroutes app
  (GET "/" [] "<h1>Hello World</h1>")
  (POST "/settings" {body :body} (settings (body2json body)))
  (POST "/feed" {body :body} (add-feed (body2json body)))
  (route/not-found "<h1>Page not found</h1>"))

;; parser rss and send download to aria
(def interval (atom 3000))
(def should-work (atom true))

(defn query-interval []
  (let* [setting (mc/find-one-as-map db "settings" {:path "interval"})
         new-interval (if (nil? setting) 3000 (read-string (:value setting)))]
    (if (not= new-interval interval)
      (reset! interval new-interval))))

(defn start-work []
  (reset! working true))

(defn stop-work []
  (reset! working false))

(defn work []
  (println "working"))

(go
  (while true
    (query-interval)
    (<! (timeout @interval))
    (when @should-work
      (work))))
