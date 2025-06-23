(ns neo-api.components.pedestal-component
  (:require
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [io.pedestal.interceptor :as interceptor]
   [io.pedestal.http.content-negotiation :as content-negotiation]))

(defn -respond-hello [_request]
  {:status 200 :body "Hello, world!"})

(defn response [status body]
  {:status status :body body :headers nil})

(def ok (partial response 200))

(defn get-todo-by-id [{:keys [in-memory-state-component]} todo-id]
  (->> @(:state-atom in-memory-state-component)
       (filter (fn [todo]
                 (= todo-id (:id todo))))
       (first)))

(def get-todo-handler
  {:name :get-todo-handler
   :enter
   (fn [{:keys [dependencies] :as context}]

     (let [{:keys [request]} context
           response (ok
                     (get-todo-by-id dependencies (->
                                                   request
                                                   :path-params
                                                   :todo-id
                                                   parse-uuid)))]
       (assoc context :response response)))})

(def -routes
  (route/expand-routes
   #{["/greet" :get -respond-hello :route-name :greet]
     ["/todo/:todo-id" :get get-todo-handler :route-name :get-todo]}))

(def url-for (route/url-for-routes -routes))

(defn -inject-dependencies
  [dependencies]
  (interceptor/interceptor
   {:name ::inject-dependencies
    :enter (fn [context] (assoc context :dependencies dependencies))}))

(def content-negotiation-interceptor
  (content-negotiation/negotiate-content ["application/json"]))

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
                     (update
                      ::http/interceptors concat
                      [(-inject-dependencies component)
                       content-negotiation-interceptor])
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
