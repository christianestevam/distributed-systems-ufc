# Trabalho4 — RabbitMQ + PostgreSQL + API + Worker + Publisher

Este repositório contém um exemplo com três serviços: `api-service`, `worker-service` e `publisher-service`, orquestrados por Docker Compose. Inclui suporte a DLQ (dead-letter queue) para mensagens que falham no processamento.

## Pré-requisitos
- Docker & Docker Compose (v2)
- Java 17
- Maven
- (Opcional) DBeaver para inspecionar o banco

## Ports importantes
- API: http://localhost:8080
- Publisher: http://localhost:8090
- RabbitMQ management: http://localhost:15672 (user: `trabalho4`, pass: `trabalho4`)
- Postgres (host): localhost:5433

## Build e executar localmente (Docker Compose)
No diretório `trabalho4`:

```bash
cd /Users/christianestam/ufc/distributed-systems-ufc/trabalho4
# Build imagens (após alterações de código)
docker compose build --no-cache
# Iniciar tudo em background
docker compose up -d
# Ver status
docker compose ps
```

Para reiniciar apenas alguns serviços:

```bash
docker compose restart publisher-service worker-service
```

Para derrubar os serviços e remover containers:

```bash
docker compose down
```

## Rodar todos os testes (Maven)
Cada serviço é um projeto Maven separado. Execute os testes unitários em cada serviço com:

```bash
cd /Users/christianestam/ufc/distributed-systems-ufc/trabalho4
for svc in api-service worker-service publisher-service; do
  echo "Running tests in $svc"
  (cd $svc && mvn test)
done
```

Ou execute em paralelo (se preferir):

```bash
(cd api-service && mvn -T1C test) &
(cd worker-service && mvn -T1C test) &
(cd publisher-service && mvn -T1C test) &
wait
```

Observação: esses comandos apenas executam os testes unitários/internos dos módulos. Para testes de integração end-to-end, suba o Compose e siga a seção abaixo.

## Teste end-to-end e apresentação (fluxo completo)
1. Subir o ambiente:

```bash
docker compose up -d --build
```

2. Publicar um lote de mensagens de teste (publisher):

```bash
# envia 10 mensagens de teste (ajuste quantidade)
curl -s -X POST "http://localhost:8090/api/publisher/teste?quantidade=10"
```

3. Verificar que o `worker-service` consumiu e que a API persiste/expõe as mensagens:

```bash
# listar via API
curl -s http://localhost:8080/api/mensagens | jq '.'
# checar diretamente no Postgres
docker compose exec postgres psql -U trabalho4 -d trabalho4 -c "SELECT id, ticker, preco, volume, status, received_at FROM mensagem_processada ORDER BY received_at DESC LIMIT 10;"
```

4. Apresentando:
- Abra o RabbitMQ Management em `http://localhost:15672` (user/password: `trabalho4`). Mostre as filas `bolsa.mensagens` e `bolsa.mensagens.dlq`.
- Mostre a publicação do `publisher-service` (endpoint `/api/publisher/teste`).
- Mostre o consumo do `worker-service` pelos logs:

```bash
docker compose logs --follow worker-service
```

- Mostre os dados persistidos pela `api-service` via endpoint `/api/mensagens`.

## Teste do DLQ (dead-letter queue)
Para demonstrar a DLQ, force o envio de uma mensagem que falhe no processamento (por exemplo, payload inválido). O `worker-service` está configurado para lançar `AmqpRejectAndDontRequeueException` em falhas, fazendo com que o broker mova a mensagem para a DLQ `bolsa.mensagens.dlq`.

1. Enviar uma mensagem inválida:

```bash
# Exemplo: payload não-JSON ou JSON inválido
curl -s -X POST http://localhost:8090/api/publisher/publicar \
  -H "Content-Type: application/json" \
  -d 'INVALID_JSON'
```

ou (JSON mas com campos ausentes):

```bash
curl -s -X POST http://localhost:8090/api/publisher/publicar \
  -H "Content-Type: application/json" \
  -d '{}'
```

2. Verificar DLQ via HTTP API do RabbitMQ:

```bash
# lista a fila DLQ com contagem de mensagens
curl -sS -u trabalho4:trabalho4 http://localhost:15672/api/queues/%2F/bolsa.mensagens.dlq | jq '.'
```

3. Recuperar (ler e opcionalmente reencaminhar) mensagens da DLQ usando HTTP API:

```bash
# pega uma mensagem da DLQ e a reencaminha (requeue=true) para tentar processar novamente
curl -sS -u trabalho4:trabalho4 -H "content-type:application/json" -X POST http://localhost:15672/api/queues/%2F/bolsa.mensagens.dlq/get -d '{"count":1,"requeue":true,"encoding":"auto"}' | jq '.'

# pegar sem reenfileirar (remove da DLQ)
curl -sS -u trabalho4:trabalho4 -H "content-type:application/json" -X POST http://localhost:15672/api/queues/%2F/bolsa.mensagens.dlq/get -d '{"count":1,"requeue":false,"encoding":"auto"}' | jq '.'
```
## Comandos úteis de depuração durante apresentação
```bash
# ver containers
docker compose ps
# ver logs (follow)
docker compose logs -f api-service publisher-service worker-service
# checar filas via API
curl -sS -u trabalho4:trabalho4 http://localhost:15672/api/queues/%2F | jq -r '.[] | .name + " ready=" + (.messages_ready|tostring) + " unacked=" + (.messages_unacknowledged|tostring)'
# listar tabelas no Postgres
docker compose exec postgres psql -U trabalho4 -d trabalho4 -c "\dt"
```

## Visão do projeto (README do projeto)

Este README dá uma visão do projeto, descreve a arquitetura, componentes, como rodar, testar, demonstrar o DLQ e onde olhar logs/erros durante a apresentação.

### Objetivo
Implementar um pipeline simples de mensagens para processar dados da bolsa:
- Publicar mensagens (publisher)
- Consumir e processar em background (worker)
- Persistir em PostgreSQL
- Expor via REST (API)
- Garantir mensagens com falha sejam enviadas à DLQ para inspeção/reprocessamento

### Componentes e responsabilidades
- `publisher-service`: gera/recebe payloads e publica mensagens em `bolsa.mensagens`.
- `worker-service`: consome `bolsa.mensagens`, valida/processa e persiste registros em `mensagem_processada` no Postgres. Em erro, rejeita a mensagem para a DLQ `bolsa.mensagens.dlq`.
- `api-service`: endpoints HTTP para consultar mensagens processadas e estatísticas.
- `rabbitmq`: broker (plugin management ativo) — filas, exchanges, DLQ.
- `postgres`: armazena mensagens processadas via JPA/Hibernate.

### Estrutura de pastas (resumo)
- `api-service/` — Spring Boot REST API
- `worker-service/` — Spring Boot Rabbit listener e JPA entities
- `publisher-service/` — utilitário/endpoint para publicar mensagens
- `docker-compose.yml` — orquestração dos serviços
- `README.md` — este arquivo

## Configurações importantes
- Queue name: `bolsa.mensagens`
- DLQ name: `bolsa.mensagens.dlq`
- Postgres (container -> host): `5432` -> `5433` (host)
- RabbitMQ management: `15672` (user/pass: `trabalho4`)

## Testes

1) Testes unitários (cada serviço):

```bash
cd trabalho4
for svc in api-service worker-service publisher-service; do
  (cd $svc && mvn test)
done
```

2) Testes de integração / end-to-end (manual ou script):

- Suba o ambiente com `docker compose up -d --build`.
- Use o endpoint do `publisher-service` para enviar uma massa de teste:

```bash
curl -s -X POST "http://localhost:8090/api/publisher/teste?quantidade=20"
```

- Verifique logs do `worker-service` para consumo e persista:

```bash
docker compose logs --follow worker-service
```

- Confirme via API:

```bash
curl -s http://localhost:8080/api/mensagens | jq '.'
```

3) Teste DLQ (verificação prática):

- Envie uma mensagem malformada:

```bash
curl -s -X POST http://localhost:8090/api/publisher/publicar -H "Content-Type: application/json" -d 'INVALID_JSON'
```

- Verifique a fila DLQ no management ou via HTTP API:

```bash
curl -sS -u trabalho4:trabalho4 http://localhost:15672/api/queues/%2F/bolsa.mensagens.dlq | jq '.'
```

## Apresentação (roteiro curto)
1. Mostrar arquitetura (árvore de componentes e responsabilidades).
2. Subir ambiente rápido:`docker compose up -d --build`.
3. Mostrar RabbitMQ Management com filas `bolsa.mensagens` e `bolsa.mensagens.dlq`.
4. Executar publish de teste (`/api/publisher/teste`) e acompanhar logs do worker.
5. Consultar a API para mostrar registros persistidos (`/api/mensagens`).
6. Forçar falha, demonstrar entrada na DLQ e explicitar estratégias de reprocessamento.

## Comandos úteis de verificação

```bash
cd /Users/christianestam/ufc/distributed-systems-ufc/trabalho4
docker compose ps
docker compose logs -f worker-service
curl -sS -u trabalho4:trabalho4 http://localhost:15672/api/queues/%2F | jq -r '.[] | .name + " ready=" + (.messages_ready|tostring) + " unacked=" + (.messages_unacknowledged|tostring)'
docker compose exec postgres psql -U trabalho4 -d trabalho4 -c "\dt"
```

## Troubleshooting rápido
- Erro `inequivalent arg 'x-dead-letter-exchange'`: delete a fila antiga via API ou pelo management e reinicie o serviço para que a fila seja recriada com os novos argumentos.
- Erro de autenticação RabbitMQ: verifique `SPRING_RABBITMQ_USERNAME`/`PASSWORD` no `docker-compose.yml`.
- Port conflicts: Postgres foi mapeado para host `5433`.

## Próximos passos e sugestões
- Adicionar endpoint administrativo para reprocessamento (replay) da DLQ.
- Adicionar métricas e alertas para mensagens enviadas à DLQ.

---
Atualizo a tarefa e marco como concluída agora.
