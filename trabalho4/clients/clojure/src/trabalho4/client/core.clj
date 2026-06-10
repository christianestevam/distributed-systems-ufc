(ns trabalho4.client.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.pprint :refer [pprint]])
  (:import [java.net HttpURLConnection URL]))

(def api-base-url (or (System/getenv "API_BASE_URL") "http://localhost:8080/api"))
(def publisher-base-url (or (System/getenv "PUBLISHER_BASE_URL") "http://localhost:8090/api/publisher"))

(defn- http-request [url method body]
  (let [^HttpURLConnection conn (.openConnection (URL. url))]
    (.setRequestMethod conn method)
    (.setRequestProperty conn "Accept" "application/json")
    (when body
      (.setDoOutput conn true)
      (.setRequestProperty conn "Content-Type" "application/json")
      (with-open [writer (java.io.OutputStreamWriter. (.getOutputStream conn) "UTF-8")]
        (.write writer (json/write-str body))))
    conn))

(defn- call-api [url method body]
  (let [^HttpURLConnection conn (http-request url method body)
        status (.getResponseCode conn)
        stream (if (>= status 400) (.getErrorStream conn) (.getInputStream conn))
        payload (if stream (slurp stream) "")]
    (when (>= status 400)
      (throw (ex-info payload {:status status})))
    (when (seq payload)
      (json/read-str payload :key-fn keyword))))

(defn- prompt [text]
  (print text)
  (flush)
  (read-line))

(defn -main [& _args]
  (loop []
    (println "==========================================")
    (println "Cliente Clojure - Trabalho 4")
    (println "1. Listar mensagens processadas")
    (println "2. Total de mensagens")
    (println "3. Publicar lote de teste")
    (println "0. Sair")
    (let [option (prompt "Opção: ")]
      (cond
        (= option "1") (pprint (call-api (str api-base-url "/mensagens") "GET" nil))
        (= option "2") (pprint (call-api (str api-base-url "/mensagens/total") "GET" nil))
        (= option "3") (let [count (prompt "Quantidade (default 10): ")]
                           (pprint (call-api (str publisher-base-url "/teste?quantidade=" (or (not-empty count) "10")) "POST" nil)))
        (= option "0") (do (println "Saindo...") (System/exit 0))
        :else (println "Opção inválida.")))
    (recur)))(ns trabalho4.client.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.pprint :refer [pprint]])
  (:import [java.net HttpURLConnection URL]))

(def api-base-url
  (or (System/getenv "API_BASE_URL")
      "http://localhost:8080/api"))

(def publisher-base-url
  (or (System/getenv "PUBLISHER_BASE_URL")
      "http://localhost:8082/api"))

(defn- request [url method body]
  (let [^HttpURLConnection connection (.openConnection (URL. url))]
    (.setRequestMethod connection method)
    (.setRequestProperty connection "Accept" "application/json")
    (when body
      (.setDoOutput connection true)
      (.setRequestProperty connection "Content-Type" "application/json")
      (with-open [writer (java.io.OutputStreamWriter. (.getOutputStream connection) "UTF-8")]
        (.write writer (json/write-str body))))
    connection))

(defn- call-api [url method body]
  (let [^HttpURLConnection connection (request url method body)
        status (.getResponseCode connection)
        stream (if (>= status 400) (.getErrorStream connection) (.getInputStream connection))
        raw-body (if stream (slurp stream) "")]
    (when (>= status 400)
      (throw (ex-info (str status " " raw-body) {:status status :body raw-body})))
    (when (seq raw-body)
      (json/read-str raw-body :key-fn keyword))))

(defn- prompt [text]
  (print text)
  (flush)
  (read-line))

(defn- show-json [label value]
  (println)
  (println label)
  (pprint value)
  (println))

(defn- listar-mensagens []
  (show-json "Mensagens processadas" (call-api (str api-base-url "/mensagens") "GET" nil)))

(defn- listar-estatisticas []
  (show-json "Estatísticas" (call-api (str api-base-url "/estatisticas") "GET" nil)))

(defn- publicar-teste []
  (show-json "Publicação disparada"
             (call-api (str publisher-base-url "/publicar/teste?quantidade=10") "POST" nil)))

(defn -main [& _args]
  (loop []
    (println "==========================================")
    (println "Cliente Clojure - Trabalho 4")
    (println "API:" api-base-url)
    (println "1. Listar mensagens processadas")
    (println "2. Ver estatísticas")
    (println "3. Disparar massa de teste")
    (println "0. Sair")
    (let [option (prompt "Opção: ")]
      (try
        (cond
          (= option "1") (listar-mensagens)
          (= option "2") (listar-estatisticas)
          (= option "3") (publicar-teste)
          (= option "0") (do (println "Saindo...") (System/exit 0))
          :else (println "Opção inválida."))
        (catch Exception e
          (println "Erro:" (.getMessage e))))
      (recur))))
