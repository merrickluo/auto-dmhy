;; Last modified: <2017-03-10 15:53:14 Friday by merrick>

;; Copyright (C) 2017 A.I.

;; Author: A.I.
;; Email: merrick@luois.me

;; Version: 0.1
;; PUBLIC LICENSE: WTFPL
(ns dmhy.settings
  (:gen-class)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]))

(def db
  (let [conn (mg/connect)]
    (mg/get-db conn "dmhy")))

(defn update-settings [new-settings]
  (doseq [[path value] new-settings]
    (mc/update db "settings" {:path path} {:path path :value value} {:upsert true})))

(defn add-feed [feed]
  (let [url (get feed "url")]
    (mc/update db "feeds" {:url url} {:url url} {:upsert true})))

(defn all-feeds []
  (mc/find-maps db "feeds"))

(defn get-rpc []
  (mc/find-one-as-map db "settings" {:path "ariarpc"}))

(defn get-interval []
  (mc/find-one-as-map db "settings" {:path "interval"}))

(defn downloaded? [feed-url entry-title]
  (mc/any? db "feeds" {:url feed-url :did entry-title}))

(defn mark-donwload [feed-url entry-title]
  (mc/update db "feeds" {:url feed-url } {$push {:did entry-title}}))

;; settings.clj ends here
