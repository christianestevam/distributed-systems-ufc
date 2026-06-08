(ns trabalho3.client.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.pprint :refer [pprint]])
  (:import [java.net HttpURLConnection URL]))

(def api-base-url
  (or (System/getenv "API_BASE_URL")
      "http://localhost:8080/api/v1"))

(defn- request
  ([method path] (request method path nil))
  ([method path body]
   (let [connection ^HttpURLConnection (.openConnection (URL. (str api-base-url path)))]
     (.setRequestMethod connection method)
     (.setRequestProperty connection "Accept" "application/json")
     (when body
       (.setDoOutput connection true)
       (.setRequestProperty connection "Content-Type" "application/json")
       (with-open [writer (java.io.OutputStreamWriter. (.getOutputStream connection) "UTF-8")]
         (.write writer (json/write-str body))))
     connection)))

(defn- call-api
  ([method path] (call-api method path nil))
  ([method path body]
   (let [^HttpURLConnection connection (request method path body)
         status (.getResponseCode connection)
         stream (if (>= status 400)
                  (.getErrorStream connection)
                  (.getInputStream connection))
         raw-body (if stream (slurp stream) "")]
     (when (>= status 400)
       (throw (ex-info (str status " " raw-body)
                       {:status status
                        :body raw-body})))
     (when (seq raw-body)
       (json/read-str raw-body :key-fn keyword)))))

(defn- prompt [text]
  (print text)
  (flush)
  (read-line))

(defn- show-json [label value]
  (println)
  (println label)
  (pprint value)
  (println))

(defn- criar-investidor []
  (let [investidor-id (prompt "Investidor ID: ")
        nome (prompt "Nome: ")
        cpf (prompt "CPF: ")
        email (prompt "E-mail: ")
        telefone (prompt "Telefone: ")]
    (show-json "Investidor criado"
               (call-api "POST" "/investidores"
                         {:investidorId investidor-id
                          :nome nome
                          :cpf cpf
                          :email email
                          :telefone telefone}))))

(defn- obter-investidor []
  (let [investidor-id (prompt "Investidor ID: ")]
    (show-json "Investidor encontrado"
               (call-api "GET" (str "/investidores/" investidor-id)))))

(defn- criar-ordem []
  (let [investidor-id (prompt "Investidor ID: ")
        ordem-id (prompt "Ordem ID: ")
        tipo (prompt "Tipo (COMPRA/VENDA): ")
        ticker (prompt "Ticker: ")
        quantidade (Long/parseLong (prompt "Quantidade: "))
        preco-unitario (Double/parseDouble (prompt "Preço unitário: "))]
    (show-json "Ordem criada"
               (call-api "POST" (str "/investidores/" investidor-id "/ordens")
                         {:ordemId ordem-id
                          :tipo tipo
                          :ticker ticker
                          :quantidade quantidade
                          :precoUnitario preco-unitario}))))

(defn- obter-ordens []
  (let [investidor-id (prompt "Investidor ID: ")]
    (show-json "Ordens do investidor"
               (call-api "GET" (str "/investidores/" investidor-id "/ordens")))))

(defn- adicionar-saldo []
  (let [investidor-id (prompt "Investidor ID: ")
        valor (Double/parseDouble (prompt "Valor: "))]
    (show-json "Saldo atualizado"
               (call-api "POST" (str "/investidores/" investidor-id "/saldo")
                         {:valor valor}))))

(defn- obter-ativo []
  (let [ticker (prompt "Ticker: ")]
    (show-json "Ativo encontrado"
               (call-api "GET" (str "/ativos/" ticker)))))

(defn- listar-ativos []
  (show-json "Ativos disponíveis"
             (call-api "GET" "/ativos")))

(defn -main [& _args]
  (loop []
    (println "==========================================")
    (println "Cliente Clojure - Trabalho 3")
    (println "API:" api-base-url)
    (println "1. Criar investidor")
    (println "2. Obter investidor")
    (println "3. Criar ordem")
    (println "4. Obter ordens do investidor")
    (println "5. Adicionar saldo à carteira")
    (println "6. Obter ativo")
    (println "7. Listar ativos")
    (println "0. Sair")
    (let [opcao (prompt "Opção: ")]
      (try
        (cond
          (= opcao "1") (criar-investidor)
          (= opcao "2") (obter-investidor)
          (= opcao "3") (criar-ordem)
          (= opcao "4") (obter-ordens)
          (= opcao "5") (adicionar-saldo)
          (= opcao "6") (obter-ativo)
          (= opcao "7") (listar-ativos)
          (= opcao "0") (do (println "Saindo...") (System/exit 0))
          :else (println "Opção inválida."))
        (catch Exception e
          (println "Erro:" (.getMessage e))))
      (recur))))