;; Last modified: <2017-03-10 15:55:26 Friday by merrick>

;; Copyright (C) 2017 A.I.

;; Author: A.I.
;; Email: merrick@luois.me

;; Version: 0.1
;; PUBLIC LICENSE: WTFPL

(ns dmhy.worker
  (:gen-class)
  (:use [dmhy.settings :as settings]
        [dmhy.aria2 :as aria2])
  (:require [feedparser-clj.core :as parser]
            [clojure.core.async :as async :refer [<! timeout chan go]]))

;; parser rss and send download to aria
(def interval (atom 3000))
(def should-work (atom true))

(defn query-interval []
  (let* [setting (settings/get-interval)
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
        (when-not (settings/downloaded? url entry-title)
          (try
            (aria2/send-uri entry-url)
            (settings/mark-donwload url entry-title)))))))

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
  (let [feeds (settings/all-feeds)]
    (doseq [feed feeds]
      (refresh-feed feed))))

(go (while true
      (query-interval)
      (<! (timeout @interval))
      (when @should-work
        (work))))

;; worker.clj ends here
