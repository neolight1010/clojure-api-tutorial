(ns neo-api.components.pedestal-component
  (:require
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [io.pedestal.interceptor :as interceptor]))

(defn -respond-hello [_request]
  {:status 200 :body "Hello, world!"})

(defn response [status body]
  {:status status :body body :headers nil})

(def ok (partial response 200))

(def get-todo-handler
  {:name :echo
   :enter
   (fn [context]
     (let [_request (:request context)
           response (ok context)]
       (assoc context :response response)))})

(def -routes
  (route/expand-routes
   #{["/greet" :get -respond-hello :route-name :greet]
     ["/todo/:list-id" :get get-todo-handler :route-name :get-todo]}))

(defn -inject-dependencies
  [dependencies]
  (interceptor/interceptor
   {:name ::inject-dependencies
    :enter (fn [context] (assoc context :dependencies dependencies))}))

(defrecord PedestalComponent
           [config
            example-component
            in-memory-state-component]
  component/Lifecycle

  (start [component]
    (println ";; Starting PedestalComponent")
    (println ";; Config:" config)
    (let [server (-> {::http/routes -routes
                      ::http/type :jetty
                      ::http/join? false
                      ::http/port (-> config :server :port)}
                     (http/default-interceptors)
                     (update ::http/interceptors concat [(-inject-dependencies component)])
                     (http/create-server)
                     (http/start))]
      (assoc component :server server)))

  (stop [component]
    (println ";; Stopping PedestalComponent")
    (when-let [server (:server component)]
      (http/stop server))
    (assoc component :server nil)))

(defn new-pedestal-component [config]
  (map->PedestalComponent {:config config}))
