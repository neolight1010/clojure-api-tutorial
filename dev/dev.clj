(ns dev
  (:require [com.stuartsierra.component.repl :as component-repl]
            [neo-api.core :as core]))

(component-repl/set-init
 (fn [_old-system]
   (core/api-system {:server {:port 3001}})))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(def reset component-repl/reset)
