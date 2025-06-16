(ns unit.neo-api.simple-test
  (:require
   [clojure.test :as test]
   [neo-api.components.pedestal-component :refer [url-for]]))

(test/deftest url-for-test
  (test/testing "greet endpoint url"
    (test/is (= "/greet" (url-for :greet)))

    (test/testing "get ttodo by id endpoint url"
      (let [todo-id (random-uuid)]
        (test/is
         (= (str "/todo/" todo-id)
            (url-for :get-todo {:path-params {:todo-id todo-id}})))))))
