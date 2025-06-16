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

  (let [todo-id-1 (random-uuid)
        todo-1 {:id todo-id-1
                :name "todo 1"
                :items [{:id (random-uuid) :name "item"}]}]
    (with-system
      [sut (core/api-system {:server {:port 8088}})]
      (reset! (-> sut :in-memory-state-component :state-atom) [todo-1])

      (test/is (= {:body (pr-str todo-1) :status 200}
                  (-> (str "http://localhost:" 8088 "/todo/" todo-id-1)
                      (client/get)
                      (select-keys [:body :status]))))

      (test/testing "empty body is returned for random todo-id"
        (= {:body "" :status 200}
           (-> (str "http://localhost:" 8088 "/todo/" (random-uuid))
               (client/get)
               (select-keys [:body :status])))))))


