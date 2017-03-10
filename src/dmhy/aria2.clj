;; Last modified: <2017-03-10 16:01:00 Friday by merrick>

;; Copyright (C) 2017 A.I.

;; Author: A.I.
;; Email: merrick@luois.me

;; Version: 0.1
;; PUBLIC LICENSE: WTFPL

(ns dmhy.aria2
  (:use [dmhy.settings :as settings])
  (:require [clojure.data.json :as json]
            [clj-http.client :as client])
  (:gen-class))

(defn parse-rpcpath [rpcpath]
  (let [token (last (re-matches #".*/(token.*)@.*" rpcpath))
        url (clojure.string/replace rpcpath (str token "@") "")]
    [url token]))

(defn send-uri [download-uri]
  (let [rpc (settings/get-rpc)
        [url token] (parse-rpcpath (:value rpc))
        body (json/write-str
              {:jsonpc "2.0"
               :id "1"
               :method "aria2.addUri"
               :params [token [download-uri]]})]
    (try
      (client/post url {:body body}))))

;; aria2.clj ends here
