(ns neo-api.core
  (:require
   [com.stuartsierra.component :as component]
   [neo-api.components.example-component :as example-component]
   [neo-api.components.pedestal-component :as pedestal-component]
   [neo-api.config :as config]))

(defn api-system [config]
  (component/system-map
   :example-component (example-component/new-example-component config)
   :pedestal-component (component/using (pedestal-component/new-pedestal-component config) [:example-component])))

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
