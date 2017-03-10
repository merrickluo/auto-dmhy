(ns dmhy.core
  (:gen-class)
  (:use [dmhy.settings :as settings]
        [dmhy.aria2 :as aria2]
        [dmhy.worker :as worker])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.data.json :as json]))

(defn settings-handler
  "change settings"
  [req]
  (settings/update-settings req)
  "ok")

(defn feed-handler
  "add dmhy rss feed"
  [req]
  (settings/add-feed req)
  "ok")

(defn body2json
  "parse request body to json"
  [body]
  (json/read-str (slurp body)))

(defroutes app
  (GET "/" [] "<h1>Hello World</h1>")
  (POST "/settings" {body :body} (settings-handler (body2json body)))
  (POST "/feed" {body :body} (feed-handler (body2json body)))
  (route/not-found "<h1>Page not found</h1>"))

(worker/start-work)
