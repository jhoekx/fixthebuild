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

(defn- save-person [name mail]
  (let [person {:name name
                :mail mail}]
    (go
      (<! (http/post "api/person" {:json-params person}))
      (get-persons))))

(defn- remove-person [{uuid :uuid}]
  (go
    (<! (http/delete (str "api/person/" uuid)))
    (get-persons)))

(defn- fix-build [{uuid :uuid}]
  (go
    (<! (http/post (str "api/person/" uuid)))
    (get-persons)))

(defn person-display [person]
  [:form {:on-submit (fn [e]
                       (remove-person person)
                       (.preventDefault e))}
   [:span (:name person) " <" (:mail person) ">"]
   [:input {:type  "submit"
            :value "X"}]])

(defn persons-list []
  [:ul
   (for [person @persons]
     ^{:key (:uuid person)}
     [:li [person-display person]])])

(defn- event-value [event]
  (-> event .-target .-value))

(defn persons-form []
  (let [name (r/atom "")
        mail (r/atom "")]
    (fn []
      [:form {:on-submit (fn [e]
                           (save-person @name @mail)
                           (reset! name "")
                           (reset! mail "")
                           (.preventDefault e))}
       [:fieldset
        [:legend "Add a person"]
        [:p
         [:label "Name: "
          [:input {:type      "text"
                   :value     @name
                   :required  "required"
                   :on-change #(reset! name (event-value %))}]]]
        [:p
         [:label "E-mail: "
          [:input {:type      "email"
                   :value     @mail
                   :required  "required"
                   :on-change #(reset! mail (event-value %))}]]]
        [:p
         [:input {:type "submit"}]]]])))

(defn fixer-component []
  (let [fixer (first (filter :fixer @persons))]
    [:form {:on-submit (fn [e]
                         (fix-build fixer)
                         (.preventDefault e))}
     [:p
      [:input {:type  "submit"
               :value (str (:name fixer) " fixed it!")}]]]))

(defn persons-component []
  (get-persons)
  (fn []
    [:section
     [persons-list]
     [persons-form]
     [fixer-component]]))

(r/render-component [persons-component]
                    (.getElementById js/document "app"))
