(ns ui.core
    (:require-macros
        [cljs.core.async.macros :refer [go]])
    (:require
        [reagent.core :as r :refer [atom]]
        [clojure.string :as string :refer [split-lines]]
        [goog.dom :as dom]
        [ui.data :as d]
        [ui.utils :as u]
        [ui.backend :as b]
        [cljs-http.client :as http]
        [cljs.core.async :refer [<!]]))


;Enable console logging
(enable-console-print!)

;Retrieve data
(b/get-data)

;;;~Callback to update the reminder when done editing
(defn on-reminder-update-done [new? reminder-data text-atom]
    (if new?
        ;Adding new reminder
        (when-not (empty? @text-atom)
            (b/add-reminder (d/data-get-selected-group-name) @text-atom)
            (reset! text-atom ""))
        ;Updating existing one
        (cond
            ;Removing reminder
            (empty? @text-atom)
                (b/remove-reminder (:id reminder-data))
            ;Updating reminder
            (not= (:text reminder-data) @text-atom)
                (b/update-reminder (:id reminder-data) @text-atom nil nil))))

;;;~Helper to increment or decrement a part of a dateString
(defn change-date-part [dateStr part off]
    (let [dateVec    (vec dateStr)
          dateVec    (assoc dateVec 10 "T")
          dateVec    (assoc dateVec 13 ":")
          dateStrFix (clojure.string/join "" (assoc dateVec 16 ":"))
          dateJs     (js/Date. dateStrFix)
          new-part   (+ (int (u/get-dateStr-value dateStr part)) off)]
        (u/formatDate (js/Date. (cond
            (= part :years)   (.setFullYear  dateJs new-part)
            (= part :months)  (.setMonth     dateJs (dec new-part))
            (= part :days)    (.setDate      dateJs new-part)
            (= part :hours)   (.setHours     dateJs new-part)
            (= part :minutes) (.setMinutes   dateJs new-part)
            (= part :seconds) (.setSeconds   dateJs new-part))))))

;;;~Component to display scheduled information, called by component-reminder
(defn component-schedule-part [{:keys [id scheduled]} part]
    (let [value (u/get-dateStr-value scheduled part)]
        [:span.part {:on-click        (fn [e] (.preventDefault e) (b/update-reminder id nil nil (change-date-part scheduled part 1)))
                     :on-context-menu (fn [e] (.preventDefault e) (b/update-reminder id nil nil (change-date-part scheduled part -1)))}
            value]))

;;;~Component to display a single reminder
(defn component-reminder [{:keys [id status text added scheduled priority]}]
    (let [new? (nil? id)
          reminder-data {:id id :status status :text text :added added :scheduled scheduled :priority priority}
          text-atom (r/atom text)
          w-status 30 w-schedule 260
          w-added (if (d/data-local-get-show-added) 200 0)]
        (fn []
            (println "reminder aggiornato!")
            [:div.reminder
                ;If not the new one display the status
                (when-not new? [:input.status {:type "button" :style {:color (u/stateId-to-color status) :width w-status}
                                               :value (first (u/id-to-state status))
                                               :on-click #(b/update-reminder id nil (u/change-status status) nil)}])
                ;Display text area
                [:textarea
                    {:value @text-atom :placeholder "Add reminder" :rows 1
                     :style {:width (str "calc(100% - " w-status "px - " w-schedule "px - " w-added "px)")
                             :margin-left (if new? "30px" "0px")
                             :text-decoration  (when (= status 1) "line-through" )
                             :opacity (cond (= status 2) "0.5" (= status 1) "0.25")
                             :border-bottom  (str (if new? "1px" "0px") " solid gray")
                             :color (cond (nil? @text-atom) "gray" (not= text @text-atom) "orange")}
                     :on-blur #(on-reminder-update-done new? reminder-data text-atom)
                     :on-change    #(reset! text-atom (.. % -target -value))
                     :on-key-press (fn [e] (when (= (.-charCode e) 13)
                                           (.preventDefault e)
                                           (on-reminder-update-done new? reminder-data text-atom)))}]

                ;Display schedule information if not new one
                (when-not new?
                    (if (empty? scheduled)
                        ;Empty schedule
                        [:div.schedule
                            [:input.schedule {:type "button" :value "Schedule" :style {:width w-schedule}
                                            :on-click #(b/update-reminder id nil nil (u/formatDate (js/Date.)))}]]
                        ;Scheduled date
                        [:div.schedule {:style {:width w-schedule}}
                            [component-schedule-part reminder-data :years] [:span "-"]
                            [component-schedule-part reminder-data :months] [:span "-"]
                            [component-schedule-part reminder-data :days] [:span " "]
                            [component-schedule-part reminder-data :hours] [:span ":"]
                            [component-schedule-part reminder-data :minutes] [:span ":"]
                            [component-schedule-part reminder-data :seconds]
                            [:input.cancel {:type "button" :value "X"
                                            :on-click #(b/update-reminder id nil nil "null")}]]))

                ;Display added information if not the new one
                (when (and (not new?) (d/data-local-get-show-added)) [:span.added {:style {:width w-added}} added])])))

;;;~Callback triggered when finished editing group information
(defn on-group-update-done [new? group-data name-atom]
    (if new?
        ;Adding new group
        (when-not (empty? @name-atom)
            (b/add-group @name-atom)
            (reset! name-atom ""))
        ;Updating existing one
        (cond
            ;Removing group
            (empty? @name-atom)
                (b/remove-group (:id group-data))
            ;Updating group
            (not= (:name group-data) @name-atom)
                (b/update-group (:id group-data) @name-atom))))

;;;~Special component for the new group
(defn component-group-special [name func]
    [:div.group 
        [:textarea
            {:value name
             :readOnly true
             :style {:border-bottom "0px solid gray"}
             :on-focus func}]])

;;;~Component to display group data
(defn component-group [group-data]
    (let [new? (nil? group-data)
          name (r/atom (:name group-data))]
        (fn []
            [:div.group 
                [:textarea
                    {:value @name
                     :placeholder (if new? "Add group" "")
                     :style {:border-bottom  (str (if new? "1px" "0px") " solid gray")
                             :color (cond (nil? @name) "gray" (not= (:name group-data) @name) "orange")}
                     :on-change #(reset! name (.. % -target -value))
                     :on-focus  #(d/data-set-selected-group-name (:name group-data))
                     :on-blur   #(on-group-update-done new? group-data name)
                     :on-key-press (fn [e] (when (= (.-charCode e) 13)
                                              (.preventDefault e)
                                              (on-group-update-done new? group-data name)))}]])))

;Main root component
(defn root-component []
    [:div
        [:div.app {:style {:width "100%" :height "100%" :position "absolute" :top 0 :left 0}}
            [:div
                ;Sidebar with groups
                [:div.sidebar
                    ;Special groups
                    [component-group-special "All" #(d/data-set-selected-group-name "*all*")]
                    [component-group-special "Scheduled" #(d/data-set-selected-group-name "*scheduled*")]
                    [:hr {:style {:width "100%" :border-top "0px"}}]

                    ;Custom groups
                    (for [group (d/data-get-groups)] ^{:key (:name group)}
                        [component-group group])

                    ;New group
                    [component-group nil]]

                ;Main reminder list
                [:div.list 
                    ;Header
                    [:h1 (d/data-get-selected-group-name)]
                    [:input {:type "button" :value "[Toggle added]" :style {:float "right"}
                             :on-click #(d/data-local-set-show-added (not (d/data-local-get-show-added)))}]

                    ;Grouped Reminders
                    (let [grouped (u/group-by-day (d/data-get-selected-group))
                          unscheduled-group (first (filter #(nil? (first %)) grouped))
                          scheduled-groups   (filter #(not (nil? (first %))) grouped)]

                        [:div
                            ;Scheduled reminders
                            (doall(for [reminders-for-day scheduled-groups] ^{:key (str (rand-int 999999999) (d/data-local-get-show-added))}
                                [:div.timegroup 
                                    [:span.timegrouptime (u/get-day (:scheduled (first (second reminders-for-day))))]
                                    (for [reminder (second reminders-for-day)] ^{:key (str reminder)}
                                        [component-reminder reminder])]))
                            ;Unscheduled reminders
                            [:div.timegroup 
                                [:span.timegrouptime "Unscheduled"]
                                (doall(for [reminder (second unscheduled-group)] ^{:key (str reminder (d/data-local-get-show-added))}
                                    [component-reminder reminder]))]])

                    ;Special new reminder
                    [component-reminder nil]]]]])

;Main render function
(r/render
    [root-component]
    (js/document.getElementById "app-container"))
