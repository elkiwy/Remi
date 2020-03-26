(ns ui.data
    (:require
        [reagent.core :as r :refer [atom]]
        [clojure.string :as string :refer [split-lines]]))



(def data-model (r/atom {}))
(def local-data-model (r/atom {}))


;;;~Get all the groups data
(defn data-get-groups []
    (get-in @data-model [:groups]))

;;;~Get all the reminders data from a group
(defn data-get-reminders [group-name]
    (first (filter #(= (:name %) group-name) (data-get-groups))))

;;;~Set the currently selected group
(defn data-set-selected-group-name [name]
    (swap! data-model assoc-in [:selected-group] name))

;;;~Get all the reminders data
(defn data-get-all-reminders []
    {:reminders (reduce concat (map :reminders (data-get-groups)))})

;;;~Get all the scheduled reminders
(defn data-get-scheduled-reminders []
    (let [all      (reduce concat (map :reminders (data-get-groups)))
          filtered (filter #(not-empty (:scheduled %)) all)
          sorted   (sort-by :scheduled filtered)]
        {:reminders sorted}))

;;;~Get the selected group name
(defn data-get-selected-group-name []
    (get-in @data-model [:selected-group]))

;;;~Get the selected group data
(defn data-get-selected-group []
    (let [name (data-get-selected-group-name)]
        (cond (= name "*all*")
                (data-get-all-reminders)
              (= name "*scheduled*")
                (data-get-scheduled-reminders)
              :else
                (first (filter #(= (:name %) name) (data-get-groups))))))

;;;~Initiliaze date model
(defn data-init []
    (reset! local-data-model {:show-added false})
    (reset! data-model {:groups []}))
(data-init)

;;;~Load data into data model
(defn data-load [data]
    (let [sel-group (data-get-selected-group-name)]
        (reset! data-model data)
        (if (nil? sel-group)
            (data-set-selected-group-name (get-in data [:groups 0 :name]))
            (swap! data-model assoc :selected-group sel-group))))

;;;~Set show added variable value
(defn data-local-set-show-added [visible?]
    (swap! local-data-model assoc-in [:show-added] visible?))

;;;~Get show added variable value
(defn data-local-get-show-added []
    (get-in @local-data-model [:show-added]))
