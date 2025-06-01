(ns component.neo-api.api-test
  (:require [clojure.test :as test]
            [neo-api.core :as core]
            [com.stuartsierra.component :as component]))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(test/deftest greeting-test
  (declare sut)
  (with-system [sut (core/api-system {:server {:port 8088}})]
    (test/is (= 0 1))))
