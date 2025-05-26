(ns neo-api.core
  (:require
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [neo-api.components.example-component :as example-component]
   [neo-api.config :as config]))

(defn -respond-hello [_request]
  {:status 200 :body "Hello, world!"})

(def -routes
  (route/expand-routes
   #{["/greet" :get -respond-hello :route-name :greet]}))

(defn -create-server [config]
  (http/create-server
   {::http/routes -routes
    ::http/type :jetty
    ::http/join? false
    ::http/port (-> config :server :port)}))

(defn -start-server [config]
  (http/start (-create-server config)))

(defn api-system [config]
  (component/system-map
   :example-component (example-component/new-example-component config)))

(defn -main
  []
  (let [system  (->
                 (config/read-config)
                 (api-system)
                 (component/start-system))]
    (println "🚀 starting server")
    (println "config:" system)

    (.addShutdownHook
     (Runtime/getRuntime)
     (new Thread #(component/stop-system system)))))
