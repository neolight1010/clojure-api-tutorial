(ns component.neo-api.api-test
  (:require
   [clj-http.client :as client]
   [clojure.test :as test]
   [com.stuartsierra.component :as component]
   [neo-api.components.pedestal-component :refer [url-for]]
   [neo-api.core :as core])
  (:import
   [java.net ServerSocket]))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(defn sut->url
  [sut path]
  (str "http://localhost:"
       (-> sut :pedestal-component :config :server :port)
       (if (= (first path) "/")
         path
         (str "/" path))))

(defn sut->url-for
  [sut name & options]
  (sut->url sut (apply url-for name options)))

(defn get-free-port
  []
  (with-open [socket (ServerSocket. 0)] (.getLocalPort socket)))

(test/deftest greeting-test
  (declare sut)
  (with-system
    [sut (core/api-system {:server {:port (get-free-port)}})]
    (test/is (= {:body "Hello, world!" :status 200}
                (-> (sut->url-for sut :greet)
                    (client/get)
                    (select-keys [:body :status]))))))

(test/deftest content-negotiation-test
  (test/testing "only application/json is accepted")
  (declare sut)
  (with-system
    [sut (core/api-system {:server {:port (get-free-port)}})]
    (test/is (= {:body "Not Acceptable" :status 406}
                (-> (sut->url-for sut :greet)
                    (client/get {:accept :edn :throw-exceptions false})
                    (select-keys [:body :status]))))))

(test/deftest get-todo-test
  (declare sut)

  (let [todo-id-1 (str (random-uuid))
        todo-1 {:id todo-id-1
                :name "todo 1"
                :items [{:id (str (random-uuid)) :name "item"}]}]
    (with-system
      [sut (core/api-system {:server {:port (get-free-port)}})]
      (reset! (-> sut :in-memory-state-component :state-atom) [todo-1])

      (test/is (= {:body todo-1 :status 200}
                  (-> (sut->url-for sut :get-todo {:path-params {:todo-id todo-id-1}})
                      (client/get {:as :json})
                      (select-keys [:body :status]))))

      (test/testing "empty body is returned for random todo-id"
        (= {:body "" :status 404}
           (-> (sut->url-for sut :get-todo {:path-params {:todo-id (random-uuid)}})
               (client/get {:throw-exceptions false})
               (select-keys [:body :status])))))))
