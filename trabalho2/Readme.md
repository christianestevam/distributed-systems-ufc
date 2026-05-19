# Trabalho 2 – Remote Method Invocation (RMI)

## Visão Geral

Implementação de um sistema distribuído baseado em **Remote Method Invocation (RMI)** usando **protocolo UDP** para comunicação requisição-resposta. O sistema simula um serviço de gestão de investimentos remotos.

### Requisitos Atendidos

✓ **6 classes de entidades** (Ativo, AtivoB3, AtivoFixo, Investidor, Carteira, Conta)
✓ **2+ composições tipo agregação** ("tem-um"): Investidor → Carteira; Investidor → Conta
✓ **2+ composições tipo extensão** ("é-um"): Ativo → AtivoB3; Ativo → AtivoFixo
✓ **6 métodos remotos** (mínimo 4): criarInvestidor, obterInvestidor, criarOrdem, obterOrdensDoInvestidor, adicionarSaldoCarteira, obterAtivo
✓ **Passagem por referência**: RemoteObjectRef
✓ **Passagem por valor**: serialização em Java native (ObjectInputStream/ObjectOutputStream)
✓ **Sem sockets TCP**: protocolo implementado com UDP

---

## Arquitetura do Sistema

### Estrutura de Entidades

```
Ativo (abstrata)
├─ AtivoB3 ("é-um")
│  └─ ticker, segmento, volumeNegociado
└─ AtivoFixo ("é-um")
   └─ taxaJuros, dataVencimento

Investidor (entidade principal)
├─ carteira: Carteira ("tem-um") [agregação]
│  └─ Map<ticker, quantidade>
│  └─ saldoDisponivel
└─ contas: Conta ("tem-um") [agregação]

OrdemInvestimento
└─ tipo, ticker, quantidade, precoUnitario, status

Conta
└─ banco, agencia, numeroConta, saldo
```

### Protocolo RMI (UDP)

```
┌──────────────────────────────────────────┐
│  Estrutura de Mensagem                    │
├──────────────────────────────────────────┤
│  messageType    [int: 0=Req, 1=Reply]    │
│  requestId      [int]                     │
│  objectReference[String]                  │
│  methodId       [String]                  │
│  arguments      [byte[]]                  │
└──────────────────────────────────────────┘
```

---

## Implementação do Protocolo RMI

### 1) RMIRequest e RMIReply

```java
public class RMIRequest implements Serializable {
    private int messageType;  // 0 = Request
    private int requestId;
    private String objectReference;  // nome do objeto
    private String methodId;         // nome do método
    private byte[] arguments;        // argumentos serializados
}

public class RMIReply implements Serializable {
    private int messageType;  // 1 = Reply
    private int requestId;
    private byte[] result;    // resultado serializado
    private String exception; // mensagem de erro
    private boolean success;
}
```

### 2) RMICommunication – Três Métodos Principais

#### doOperation (cliente)
```java
public byte[] doOperation(RemoteObjectRef remoteRef, String methodId, byte[] arguments) 
        throws IOException {
    
    RMIRequest request = new RMIRequest(requestId, remoteRef.getObjectReference(), 
                                        methodId, arguments);
    byte[] requestBytes = serializeRequest(request);
    
    // Envia via UDP
    DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length,
                                                remoteRef.getHostAddress(), 
                                                remoteRef.getPort());
    socket.send(packet);
    
    // Aguarda resposta
    DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
    socket.receive(responsePacket);
    
    RMIReply reply = deserializeReply(responsePacket.getData(), responsePacket.getLength());
    if (!reply.isSuccess()) {
        throw new IOException("RMI Exception: " + reply.getException());
    }
    return reply.getResult();
}
```

#### getRequest (servidor)
```java
public RMIRequest getRequest() throws IOException {
    byte[] buffer = new byte[65536];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    socket.receive(packet);
    
    RMIRequest request = deserializeRequest(packet.getData(), packet.getLength());
    request.setRequestId(packet.getPort());  // usa porta como ID
    return request;
}
```

#### sendReply (servidor)
```java
public void sendReply(byte[] replyData, InetAddress clientHost, int clientPort) 
        throws IOException {
    RMIReply reply = new RMIReply();
    reply.setResult(replyData);
    
    byte[] replyBytes = serializeReply(reply);
    DatagramPacket packet = new DatagramPacket(replyBytes, replyBytes.length,
                                                clientHost, clientPort);
    socket.send(packet);
}
```

### 3) RemoteObjectRef

```java
public class RemoteObjectRef implements Serializable {
    private String objectReference;  // nome do objeto remoto
    private InetAddress hostAddress;
    private int port;
    private String serviceVersion;
}
```

---

## Serviço Remoto (InvestidorService)

Implementa 6 métodos remotos com passagem por valor:

### Método Remoto 1: criarInvestidor
```java
public Investidor criarInvestidor(String investidorId, String nome, String cpf, 
                                  String email, String telefone) {
    Investidor inv = new Investidor(investidorId, nome, cpf, email, telefone);
    investidores.put(investidorId, inv);
    return inv;  // retorna cópia serializada
}
```

### Método Remoto 2: obterInvestidor
```java
public Investidor obterInvestidor(String investidorId) {
    return investidores.get(investidorId);  // passagem por valor
}
```

### Método Remoto 3: criarOrdem
```java
public OrdemInvestimento criarOrdem(String ordemId, String tipo, String ticker, 
                                    long quantidade, double precoUnitario) {
    OrdemInvestimento ordem = new OrdemInvestimento(ordemId, tipo, ticker, 
                                                     quantidade, precoUnitario);
    ordem.setStatus("EXECUTADA");
    ordens.put(ordemId, ordem);
    return ordem;
}
```

### Método Remoto 4: obterOrdensDoInvestidor
```java
public OrdemInvestimento[] obterOrdensDoInvestidor(String investidorId) {
    return ordens.values().stream()
        .filter(o -> o.getOrdemId().startsWith(investidorId))
        .toArray(OrdemInvestimento[]::new);
}
```

### Método Remoto 5: adicionarSaldoCarteira
```java
public double adicionarSaldoCarteira(String investidorId, double valor) {
    Investidor inv = investidores.get(investidorId);
    return inv.getCarteira().adicionarSaldo(valor);  // retorna primitivo
}
```

### Método Remoto 6: obterAtivo
```java
public Ativo obterAtivo(String ticker) {
    return ativos.get(ticker);  // retorna polimorficamente (AtivoB3 ou AtivoFixo)
}
```

---

## Compilação e Execução

### 1) Compilar (usando javac)
```bash
./compilar.sh
```

**Ou com Maven (alternativa):**
```bash
mvn clean compile
```

### 2) Executar Servidor (porta 9999)
```bash
./servidor.sh 9999
```

Saída esperada:
```
[1/3] Compilando arquivos .proto...
✓ Protocol Buffers compilados
[2/3] Verificando dependências Java...
✓ Dependências OK
[3/3] Compilando arquivos .java...
✓ Compilação bem-sucedida!
[RMIServer] Objeto remoto registrado: investidor_service
[RMIServer] Servidor RMI aguardando requisições na porta 9999...
```

### 3) Executar Cliente
```bash
./cliente.sh localhost 9999
```

---

## Estrutura de Diretórios

```
trabalho2/
├── src/main/java/br/ufc/ds/trabalho2/
│   ├── model/
│   │   ├── Ativo.java              (classe abstrata)
│   │   ├── AtivoB3.java            (extensão "é-um")
│   │   ├── AtivoFixo.java          (extensão "é-um")
│   │   ├── Investidor.java         (entidade principal)
│   │   ├── Carteira.java           (agregação "tem-um")
│   │   ├── Conta.java              (agregação "tem-um")
│   │   └── OrdemInvestimento.java
│   ├── rmi/
│   │   ├── RemoteObjectRef.java    (referência remota)
│   │   ├── RMIRequest.java         (requisição)
│   │   ├── RMIReply.java           (resposta)
│   │   ├── RMICommunication.java   (protocolo: doOperation, getRequest, sendReply)
│   │   ├── RemoteObjectManager.java
│   │   ├── RMIServer.java
│   │   └── RMIClient.java
│   └── app/
│       └── InvestidorService.java  (6 métodos remotos)
├── compilar.sh
├── servidor.sh
├── cliente.sh
└── Readme.md
```

---

## Representação Externa de Dados

Utilizamos **Protocol Buffers** para serializar/desserializar mensagens RMI e dados. Mais eficiente que serialização Java nativa.

### Arquivos .proto

Os arquivos `rmi.proto` e `model.proto` definem as estruturas de dados:

```protobuf
message RMIRequest {
    int32 messageType = 1;         // 0 = Request
    int32 requestId = 2;
    string objectReference = 3;
    string methodId = 4;
    bytes arguments = 5;
}

message RMIReply {
    int32 messageType = 1;         // 1 = Reply
    int32 requestId = 2;
    bytes result = 3;
    string exception = 4;
    bool success = 5;
}

message Ativo {
    string ticker = 1;
    double precoAtual = 2;
    string descricao = 3;
    string tipo = 4;  // "ACAO_B3" ou "RENDA_FIXA"
    oneof tipoSpecifico {
        AtivoB3 ativoB3 = 5;
        AtivoFixo ativoFixo = 6;
    }
}

message Investidor {
    string investidorId = 1;
    string nome = 2;
    string cpf = 3;
    string email = 4;
    string telefone = 5;
    Carteira carteira = 6;
    string dataCadastro = 7;
}
```

### Compilação e Serialização

```java
// Serialização com Protocol Buffers
RmiProto.RMIRequest.Builder requestBuilder = RmiProto.RMIRequest.newBuilder();
requestBuilder.setMessageType(0)
              .setRequestId(123)
              .setObjectReference("investidor_service")
              .setMethodId("criarInvestidor")
              .setArguments(com.google.protobuf.ByteString.copyFrom(argumentBytes));

RmiProto.RMIRequest request = requestBuilder.build();
byte[] serializedData = request.toByteArray();

// Desserialização
RmiProto.RMIRequest parsedRequest = RmiProto.RMIRequest.parseFrom(serializedData);
```

**Vantagens do Protocol Buffers:**
- Mais compacto que JSON/XML
- Mais eficiente que serialização Java nativa
- Suporte a versionamento backward-compatible
- Multiplataforma (suporta outras linguagens além de Java)

---

## Padrão de Composição

### Composição tipo Agregação ("tem-um")
- Investidor **tem-um** Carteira
- Investidor **tem-um** Conta

### Composição tipo Extensão ("é-um")
- AtivoB3 **é-um** Ativo
- AtivoFixo **é-um** Ativo

---

## Notas Finais

- O protocolo RMI está implementado em UDP sem sockets TCP.
- Passagem por valor: todos os argumentos e resultados são serializados e desserializados.
- Passagem por referência: via RemoteObjectRef (identificação do objeto remoto).
- Suporta múltiplos métodos remotos com diferentes assinaturas.
- Tratamento de exceções e casos de erro implementados.

---

## Referência de Compilação Manual

Se preferir compilar manualmente:

```bash
mkdir -p out
find src/main/java -name "*.java" | xargs javac -d out
```

Executar servidor:
```bash
java -cp out br.ufc.ds.trabalho2.rmi.RMIServer 9999
```

Executar cliente:
```bash
java -cp out br.ufc.ds.trabalho2.rmi.RMIClient localhost 9999
```