(ns ui.backend
    (:require-macros
        [cljs.core.async.macros :refer [go]])
    (:require
        [reagent.core :as r :refer [atom]]
        [clojure.string :as string :refer [split-lines]]
        [ui.data :as d]
        [cljs-http.client :as http]
        [cljs.core.async :refer [<!]]))

(def url "http://yourRemiBackendUrlHere.com")
(def endpoint-getData "/getData")
(def endpoint-addGroup "/addGroup")
(def endpoint-updateGroup "/updateGroup")
(def endpoint-removeGroup "/removeGroup")
(def endpoint-addReminder "/addReminder")
(def endpoint-updateReminder "/updateReminder")
(def endpoint-removeReminder "/removeReminder")

;;;~Retrieve all the data from backend
(defn get-data [ & [callback]]
    (go (let [response (<! (http/get (str url endpoint-getData) {:with-credentials? false}))]
            (println "From " endpoint-getData " got:" (:body response))
            (d/data-load (:body response))
            (when-not (nil? callback)
                (callback)))))

;;;~Add a new group
(defn add-group [group-name]
    (go (let [url-query (str url endpoint-addGroup "?name=" group-name )
              response (<! (http/get url-query {:with-credentials? false}))]
            (get-data
                #(d/data-set-selected-group-name group-name)))))

;;;~Update a group data
(defn update-group [id name]
    (go (let [url-query (str url endpoint-updateGroup "?id=" id "&name=" name)
              response (<! (http/get url-query {:with-credentials? false}))]
            (println "From " url-query " got:" (:body response))
            (get-data))))

;;;~Remove a group
(defn remove-group [group-id]
    (go (let [url-query (str url endpoint-removeGroup "?id=" group-id )
              response (<! (http/get url-query {:with-credentials? false}))]
            (get-data
                #(d/data-set-selected-group-name (get-in (d/data-get-groups) [0 :name]))
                ))))

;;;~Add a new reminder
(defn add-reminder [group-name text]
    (go (let [url-query (str url endpoint-addReminder "?group=" group-name "&text=" text)
              response (<! (http/get url-query {:with-credentials? false}))]
            (get-data))))

;;;~Update reminder data
(defn update-reminder [id text status scheduled]
    (go (let [url-query (str url endpoint-updateReminder "?id=" id )
              url-query (if (nil? text) url-query (str url-query "&text=" text))
              url-query (if (nil? scheduled) url-query (str url-query "&scheduled=" scheduled))
              url-query (if (nil? status) url-query (str url-query "&status=" status))
              response (<! (http/get url-query {:with-credentials? false}))]
            (println "From " url-query " got:" (:body response))
            (get-data))))

;;;~Remove a reminder
(defn remove-reminder [id]
    (go (let [url-query (str url endpoint-removeReminder "?id=" id )
              response (<! (http/get url-query {:with-credentials? false}))]
            (println "From " url-query " got:" (:body response))
            (get-data))))



