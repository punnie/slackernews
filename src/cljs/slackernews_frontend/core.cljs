(ns slackernews-frontend.core
  (:require [reagent.core :as r :refer [atom]]))

(def app-db (atom nil))

(defn app-container
  ""
  []
  (let [state (atom [])
        value (atom "")
        stop  #(reset! value "")
        save  #(let [v (-> @value clojure.string/trim)]
                        (if-not (empty? v)
                          (do (reset! state (conj @state @value))
                              (stop))))]
    (fn []
      [:div 
       [:input {:type "text"
                :value @value
                :on-change #(reset! value (-> % .-target .-value))
                :on-key-up #(if (= (.-which %) 13) (save))}]
       [:ul
        (for [cenas @state]
          ^{:key (rand-int 65535)} [:li {:on-click #(println (-> % .-target .-value))}
                                    cenas])]])))

(defn mount-components
  ""
  []
  (r/render-component [app-container]
                      (.getElementById js/document "app")))

(defn ^:export init!
  ""
  []
  (mount-components))
