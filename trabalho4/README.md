# Trabalho 4 - Arquitetura Orientada a Fila com RabbitMQ

Este trabalho implementa uma arquitetura baseada em fila para receber mensagens da bolsa, processá-las em segundo plano, persistir os dados em banco e expor a consulta por API HTTP.

## Arquitetura

- `publisher-service`: publica mensagens de teste na fila RabbitMQ
- `worker-service`: consome mensagens da fila e salva no PostgreSQL
- `api-service`: expõe endpoints para consultar mensagens processadas
- `rabbitmq`: broker de mensagens
- `postgres`: banco de dados

Fluxo principal:

1. O publicador envia mensagens de bolsa para a fila `bolsa.messages`.
2. O worker consome a fila, processa o JSON e grava no banco.
3. A API consulta o banco e devolve os dados em JSON.

## Como subir

1. Copie o arquivo `.env.example` para `.env`.
2. Execute:

```bash
docker compose up --build
```

## Endpoints da API

- `GET /api/health`
- `GET /api/mensagens`
- `GET /api/mensagens/{id}`
- `GET /api/mensagens/ticker/{ticker}`
- `GET /api/estatisticas`

## Publicador

- `POST /api/publicar/teste` publica uma massa de mensagens de teste
- `POST /api/publicar` publica uma mensagem única

## Clientes

Os clientes em JavaScript e Clojure consomem a API HTTP para consultar mensagens e publicar massa de teste.
