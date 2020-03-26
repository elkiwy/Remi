(ns ui.utils
    (:require
        [reagent.core :as r :refer [atom]]
        [clojure.string :as string :refer [split-lines]]))


;;;~Utility to transform a state id to a verbose string
(defn id-to-state [i]
    (cond
        (= i 0) "Todo"
        (= i 1) "Done"
        (= i 2) "Suspended"))

;;;~Utility to transform a state id to a color
(defn stateId-to-color [s]
    (cond
        (= s 0) "orange"
        (= s 1) "green"
        (= s 2) "red"))

;;;~Pretty print an object
(defn pretty-print [obj]
    (.stringify js/JSON  (clj->js obj) nil 2))

;;;~Iterate between status
(defn change-status [old-status]
    (if (< old-status 2) (inc old-status) 0))

;;;~Retrieve single data from dateString
(defn years   [dateStr] (get (clojure.string/split dateStr "-") 0))

;;;~Retrieve single data from dateString
(defn months  [dateStr] (get (clojure.string/split dateStr "-") 1))

;;;~Retrieve single data from dateString
(defn days    [dateStr] (get (clojure.string/split dateStr "-") 2))

;;;~Retrieve single data from dateString
(defn hours   [dateStr] (get (clojure.string/split dateStr "-") 3))

;;;~Retrieve single data from dateString
(defn minutes [dateStr] (get (clojure.string/split dateStr "-") 4))

;;;~Retrieve single data from dateString
(defn seconds [dateStr] (get (clojure.string/split dateStr "-") 5))

;;;~Get dateString value of a single part
(defn get-dateStr-value [dateStr key]
    (cond
        (= key :years)   (years dateStr)
        (= key :months)  (months dateStr)
        (= key :days)    (days dateStr)
        (= key :hours)   (hours dateStr)
        (= key :minutes) (minutes dateStr)
        (= key :seconds) (seconds dateStr)))

;;;~Add leading zero
(defn lz [v] (if (< v 10) (str "0" v) (str v)))

;;;~Create a dateString from a js/Date
(defn formatDate [date]
    (str (.getFullYear date) "-" (lz (inc (.getMonth date))) "-" (lz (.getDate date)) "-"
         (lz (.getHours date)) "-" (lz (.getMinutes date)) "-" (lz (.getSeconds date))))

;;;~Get the week day string from a dateString
(defn get-week-day [dateStr]
    (let [dateVec    (vec dateStr)
          dateVec    (assoc dateVec 10 "T")
          dateVec    (assoc dateVec 13 ":")
          dateStrFix (clojure.string/join "" (assoc dateVec 16 ":"))
          dateJs     (js/Date. dateStrFix)]
        (get ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"] (.getDay dateJs))))

;;;~Get only the YYYY-MM-DD from dateString
(defn get-day [dateStr]
    (clojure.string/join "" (take 10 dateStr)))

;;;~Group reminder data by day
(defn group-by-day [group-data]
    (group-by #(days (:scheduled %)) (:reminders group-data)))

