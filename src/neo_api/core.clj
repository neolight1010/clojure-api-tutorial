(ns neo-api.core
  (:require [neo-api.config :as config]))

(defn -main
  []
  (let [config  (config/read-config)]
    (println "🚀 server started")
    (println "config:" config)))
