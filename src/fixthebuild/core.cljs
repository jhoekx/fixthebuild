(ns fixthebuild.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(defonce persons (r/atom []))

(defn- get-persons []
  (go
    (let [response (<! (http/get "api/person"))]
      (reset! persons (:body response)))))

(defn- save-person [name]
  (go
    (<! (http/post "api/person" {:json-params {:name name}}))
    (get-persons)))

(defn persons-list []
  [:ul
   (for [person @persons]
     ^{:key (:name person)}
     [:li (:name person)])])

(defn persons-form []
  (let [name (r/atom "")]
    (fn []
      [:form {:on-submit (fn [e]
                           (save-person @name)
                           (reset! name "")
                           (.preventDefault e))}
       [:input {:type      "text"
                :value     @name
                :required  "required"
                :on-change #(reset! name (-> % .-target .-value))}]
       [:input {:type "submit"}]])))

(defn persons-component []
  (get-persons)
  (fn []
    [:section
     [persons-list]
     [persons-form]]))

(r/render-component [persons-component]
                    (.getElementById js/document "app"))
