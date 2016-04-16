(ns recommendation-service.handler
  (:require
    [clojure.data :refer [diff]]
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.util.response :refer [response status]]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
    [ring.middleware.cors :refer [wrap-cors]]
    [clj-http.client :as http-client]))

(defn home []
  {:message "Hello, world! This is the recommendation service."})

(defn build-url [slug]
  (str "http://localhost:3000/" slug))

(defn get-users []
  (:body (http-client/get (build-url "users") {:as :json})))

(defn get-user-by-username [username]
  (prn username)
  (:body (http-client/get (build-url (str "users/" username)) {:as :json})))

(defn get-beers []
  (:body (http-client/get "http://beers.theneva.com/beers" {:as :json})))

(defn get-recommendations [user beers]
  (prn "user" (type user) user)
  (prn "beers" (type beers) beers)
  (prn "user saved beers" (type (:beers user)) (:beers user))
  (remove nil?
          (nth (diff (:beers user) beers) 1)))

(defroutes app-routes
           (GET "/" []
             (response (home)))
           (GET "/users" []
             (response (get-users)))
           (GET "/beers" []
             (response (get-beers)))
           (GET "/users/:username" [username]
             (response (get-user-by-username username)))
           (GET "/recommendations/:username" [username]
             (response (get-recommendations
                         (get-user-by-username username)
                         (get-beers))))
           (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-cors :access-control-allow-origin "*" :access-control-allow-methods "*")
      (wrap-json-response)
      (wrap-keyword-params)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-defaults (assoc site-defaults :security false))))
