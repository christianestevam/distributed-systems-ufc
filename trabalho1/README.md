# Trabalho 1 - Sistema de Investimentos (Java)

Implementação do serviço remoto de investimentos (simulado B3). Comunicação cliente↔servidor por sockets TCP com serialização explícita usando `DataOutputStream`/`DataInputStream`.

## Estrutura principal

- Modelos (POJOs): `AtivoB3`, `Investidor`, `OrdemInvestimento`
- Streams customizados: `OrdemInvestimentoOutputStream`, `OrdemInvestimentoInputStream`
- RPC binário: `InvestmentRequest`, `InvestmentReply`, `InvestmentRpcServer`, `InvestmentRpcClient`

## Como funciona (resumo técnico)

- Protocolo: cada requisição é enviada como um payload com prefixo de 4 bytes (tamanho) seguido pelos bytes serializados.
- Serialização: `InvestmentRequest` e `InvestmentReply` usam `DataOutputStream`/`DataInputStream` (UTF, long, boolean, double).
- Servidor: `InvestmentRpcServer` é multi-threaded; cada conexão é atendida em uma nova `Thread`.

## Trechos essenciais

1) Serialização de requisição (`InvestmentRequest`)

```java
public byte[] toBytes() throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	DataOutputStream out = new DataOutputStream(baos);
	out.writeUTF(operation);
	out.writeUTF(ticker);
	out.writeLong(quantity);
	out.flush();
	return baos.toByteArray();
}

public static InvestmentRequest fromBytes(byte[] payload) throws IOException {
	DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
	String op = in.readUTF();
	String ticker = in.readUTF();
	long qty = in.readLong();
	return new InvestmentRequest(op, ticker, qty);
}
```

2) Serialização de resposta (`InvestmentReply`)

```java
public byte[] toBytes() throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	DataOutputStream out = new DataOutputStream(baos);
	out.writeBoolean(success);
	out.writeUTF(message);
	out.writeDouble(unitPrice);
	out.writeDouble(totalValue);
	out.flush();
	return baos.toByteArray();
}
```

3) Loop de aceitação e tratamento (servidor)

```java
try (ServerSocket serverSocket = new ServerSocket(port)) {
	System.out.println("InvestmentRpcServer ouvindo em " + port);
	while (true) {
		Socket socket = serverSocket.accept();
		Thread thread = new Thread(() -> handleClient(socket));
		thread.start();
	}
}
```

No `handleClient` lemos o inteiro do tamanho, em seguida `requestPayload` e convertemos com `InvestmentRequest.fromBytes(...)`.

4) Enviar requisição do cliente (exemplo simplificado)

```java
InvestmentRequest request = new InvestmentRequest("COMPRA", "PETR4", 10);
byte[] payload = request.toBytes();

try (Socket socket = new Socket(host, port);
	 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	 DataInputStream in = new DataInputStream(socket.getInputStream())) {

	out.writeInt(payload.length);
	out.write(payload);
	out.flush();

	int replySize = in.readInt();
	byte[] replyPayload = new byte[replySize];
	in.readFully(replyPayload);
	InvestmentReply reply = InvestmentReply.fromBytes(replyPayload);
}
```

5) Exemplo de stream customizado (escrevendo ordens com `OrdemInvestimentoOutputStream`)

```java
DataOutputStream out = new DataOutputStream(destino);
out.writeInt(total); // número de ordens
for (OrdemInvestimento o : ordens) {
	byte[] payload = serializarOrdem(o);
	out.writeInt(payload.length);
	out.write(payload);
}
out.flush();
```

## Como compilar e executar

Compilar (sem Maven):

```bash
./compilar.sh
```

Executar servidor (padrão porta 7070):

```bash
java -cp out br.ufc.ds.trabalho1.rpc.InvestmentRpcServer 7070
```

Executar cliente (envia uma ordem simples):

```bash
java -cp out br.ufc.ds.trabalho1.rpc.InvestmentRpcClient localhost 7070 COMPRA PETR4 10
```

## Notas finais e melhorias possíveis

- Trocar a serialização manual por Protocol Buffers ou JSON para interoperabilidade e versionamento.
- Adicionar TLS para conexões TCP em produção.
- Persistir ordens e histórico em armazenamento (arquivo/DB).

---

Se quiser, eu: crio trechos formatados em Markdown separados (por arquivo), adiciono links para os arquivos fonte no `INDEX.md` e gero scripts `iniciar-servidor.sh` / `iniciar-cliente.sh` específicos para este módulo.

## Sistema de Votações (apêndice — seguindo as questões do trabalho)

Este repositório também inclui uma implementação do sistema de votações solicitado no enunciado. Abaixo segue a explicação organizada pela ordem das questões, com trechos de código e exemplos de execução.

1) Representação externa de dados
- Implementação usada: protocolo de linha de texto simples sobre TCP para chamadas remotas (login, envio de voto, comandos administrativos). Recomenda-se protobuf para produção; JSON/XML também são aceitas.

Exemplo de payload textual (comando enviado pelo cliente ao servidor):

```text
LOGIN alice
VOTE 2
RESULTS
```

2) Comunicação unicast (TCP) para login, lista de candidatos e envio de votos
- O servidor TCP é `br.ufc.ds.trabalho1.voting.VotingServer` e o cliente é `br.ufc.ds.trabalho1.voting.VotingClient`.

Trecho do loop de aceitação (servidor):

```java
try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
	while (true) {
		Socket client = serverSocket.accept();
		new Thread(new ClientHandler(client)).start();
	}
}
```

Trecho do protocolo (no `ClientHandler`):

```java
// cliente envia: LOGIN <username>
// servidor responde: OK e depois envia lista de candidatos:
CAND_LIST <n>
CAND <id> <name>
END_LIST

// cliente envia: VOTE <id>
// servidor responde: VOTE_OK ou VOTE_REJECTED <reason>
```

3) Comunicação multicast (UDP) para notas informativas dos administradores
- O servidor envia notificações multicast via UDP quando um administrador executa `NOTIFY <mensagem>`.

Trecho de envio multicast (servidor):

```java
try (DatagramSocket ds = new DatagramSocket()) {
	byte[] buf = message.getBytes("UTF-8");
	InetAddress group = InetAddress.getByName(mcastAddr);
	DatagramPacket packet = new DatagramPacket(buf, buf.length, group, mcastPort);
	ds.send(packet);
}
```

No `VotingClient` em modo `voter` há um `MulticastSocket` ouvindo em `mcastAddr:mport` exibindo mensagens recebidas.

4) Servidor multi-threaded
- Cada conexão TCP é atendida em uma `Thread` separada (`new Thread(new ClientHandler(...)).start()`), e há um watcher que fecha as votações automaticamente quando o prazo expira.

5) Tempo máximo para envio de votos e cálculo de resultados
- Ao inicializar `VotingServer` você pode passar a duração (em segundos). Ao terminar o prazo, o servidor fecha a votação, calcula totais, percentuais e imprime o vencedor.

Exemplo de criação do servidor (executando por padrão na porta `7071` e com duração 300s):

```bash
java -cp out br.ufc.ds.trabalho1.voting.VotingServer 7071 230.0.0.0 4446 300
```

6) Administradores
- Comandos administrativos (autenticados por senha simples `adminpass` no código de exemplo):

  - `ADMIN <password>` — torna a sessão administrativa
  - `ADD <name>` — adiciona candidato (retorna id)
  - `REMOVE <id>` — remove candidato
  - `NOTIFY <message>` — envia mensagem multicast para eleitores
  - `END` — encerra votação imediatamente

Exemplo de uso (admin):

```text
ADMIN adminpass
ADD Carlos
NOTIFY Votacao encerra em 5 minutos
END
```

7) Exemplo de execução e testes

Compilar sem Maven (caso não disponha de `mvn`):

```bash
./compilar.sh
```

Iniciar servidor:

```bash
java -cp out br.ufc.ds.trabalho1.voting.VotingServer 7071 230.0.0.0 4446 300
```

Executar cliente (eleitor):

```bash
java -cp out br.ufc.ds.trabalho1.voting.VotingClient 127.0.0.1 7071 voter 230.0.0.0 4446
```

Executar cliente (admin):

```bash
java -cp out br.ufc.ds.trabalho1.voting.VotingClient 127.0.0.1 7071 admin
```

8) Observações e melhorias sugeridas
- Representação de dados: substituir protocolo textual por Protocol Buffers para chamadas remotas (mais robusto e tipado).
- Autenticação: neste exemplo a autenticação é simplificada; em produção use TLS + token/OAuth.
- Persistência: votos e candidatos são mantidos em memória; para durabilidade adicione armazenamento (arquivo/DB).

---

Se quiser, eu adapto o `ClienteMain` ou crio scripts `iniciar-votacao-servidor.sh` e `iniciar-votacao-cliente.sh` e atualizo `INDEX.md` com links. Também posso converter o protocolo para JSON/Protobuf conforme pedido do trabalho.
