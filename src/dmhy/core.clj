(ns dmhy.core
  (:gen-class)
  (:require [feedparser-clj.core :as parser]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
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
  (reset! should-work true))

(defn stop-work []
  (reset! should-work false))

(defn refresh-feed [feed]
  (prn "refreshing")
  (let* [url (:url feed)
        did (:did feed)
        entries (:entries (parser/parse-feed url))]
    (doseq [entry entries]
      (let [entry-title (:title entry)
            entry-url (:url (first (:enclosures entry)))]
        (when-not (mc/any? db "feeds" {:url url :did entry-title})
          (mc/update db "feeds" {:url url } {$push {:did entry-title}})
          (prn (str "downloading" entry-title)))))))

;; (def one-entry (first (:entries (parser/parse-feed "https://share.dmhy.org/topics/rss/rss.xml?keyword=%E5%B0%8F%E9%AD%94%E5%A5%B3%E5%AD%B8+CHS&sort_id=0&team_id=650&order=date-desc"))))
;; (let* [url  "https://share.dmhy.org/topics/rss/rss.xml?keyword=%E5%B0%8F%E9%AD%94%E5%A5%B3%E5%AD%B8+CHS&sort_id=0&team_id=650&order=date-desc"
;;        entry one-entry
;;        entry-title (:title entry)
;;        entry-url (:url (first (:enclosures entry)))]
;;   (prn entry-title)
;;   (when-not (mc/any? db "feeds" {:url url :did entry-title})
;;     (mc/update db "feeds" {:url url } {$push {:did entry-title}})
;;     (prn (str "downloading" entry-title))))

(defn work []
  (let [feeds (mc/find-maps db "feeds")]
    (doseq [feed feeds]
      (refresh-feed feed))))

(go
  (while true
    (query-interval)
    (<! (timeout @interval))
    (when @should-work
      (work))))
