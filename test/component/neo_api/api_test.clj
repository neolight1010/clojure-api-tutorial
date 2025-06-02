(ns component.neo-api.api-test
  (:require [clojure.test :as test]
            [neo-api.core :as core]
            [com.stuartsierra.component :as component]
            [clj-http.client :as client]))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(test/deftest greeting-test
  (declare sut)
  (with-system
    [sut (core/api-system {:server {:port 8088}})]
    (test/is (= {:body "Hello, world!" :status 200}
                (-> (str "http://localhost:" 8088 "/greet")
                    (client/get)
                    (select-keys [:body :status]))))))

(test/deftest get-todo-test
  (declare sut)

  (let [todo-id (random-uuid)]
    (with-system
      [sut (core/api-system {:server {:port 8088}})]
      (test/is (= {:body "Hello, world!" :status 200}
                  (-> (str "http://localhost:" 8088 "/todo/" todo-id)
                      (client/get)
                      (select-keys [:body :status])))))))
