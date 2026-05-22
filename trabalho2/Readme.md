# Trabalho 2 – Remote Method Invocation (RMI) com UDP e Protocol Buffers

## 📋 Visão Geral

Implementação de um sistema distribuído baseado em **Remote Method Invocation (RMI)** que utiliza **protocolo UDP** para comunicação requisição-resposta e **Protocol Buffers** para serialização de dados. O sistema simula um serviço de gestão de investimentos em bolsa de valores com suporte a múltiplas operações remotas.

### ✅ Requisitos Atendidos

- ✓ **6 classes de entidades**: Ativo, AtivoB3, AtivoFixo, Investidor, Carteira, OrdemInvestimento
- ✓ **2+ agregações** ("tem-um"): Investidor→Carteira, Investidor→Conta
- ✓ **2+ extensões** ("é-um"): Ativo→AtivoB3, Ativo→AtivoFixo
- ✓ **6 métodos remotos**: criarInvestidor, obterInvestidor, criarOrdem, obterOrdensDoInvestidor, adicionarSaldoCarteira, obterAtivo
- ✓ **Passagem por referência**: RemoteObjectRef (host, port, objectName)
- ✓ **Passagem por valor**: DataInputStream/DataOutputStream + Protocol Buffers
- ✓ **Protocolo UDP**: sem sockets TCP, comunicação requisição-resposta

### 🛠️ Tecnologias

- Java 11+
- Maven 3.6+
- Protocol Buffers 3.21+
- UDP (User Datagram Protocol)

---

## 🏗️ Arquitetura do Sistema

### Camadas de Comunicação

```
┌──────────────────────────────────────────────┐
│             Cliente RMI                      │
│         (RMIClient.java)                    │
└─────────────────┬──────────────────────────┘
                  │
                  │ RMIRequest (Protobuf)
                  │ UDP Datagram
                  ▼
┌──────────────────────────────────────────────┐
│        RMICommunication (UDP)                │
│  - doOperation() - envio e recebimento      │
│  - getRequest() - servidor recebe          │
│  - sendReply() - servidor envia            │
└─────────────────┬──────────────────────────┘
                  │
                  │ RMIRequest / RMIReply
                  ▼
┌──────────────────────────────────────────────┐
│          Servidor RMI                        │
│      (RMIServer.java)                       │
│  - Despachador de métodos                   │
│  - Invocador de operações                   │
└─────────────────┬──────────────────────────┘
                  │
                  │ Chamadas Locais
                  ▼
┌──────────────────────────────────────────────┐
│       InvestidorServiceImpl                   │
│    (Lógica de Negócio)                       │
└──────────────────────────────────────────────┘
```

### Modelo de Dados

```
Ativo (abstrata)
├─ AtivoB3 ("é-um")
│  └─ ticker, precoAtual, segmento, volumeNegociado
│
└─ AtivoFixo ("é-um")
   └─ ticker, precoAtual, taxaJuros, dataVencimento

Investidor (Entidade Principal)
├─ investidorId, nome, cpf, email, telefone
│
├─ carteira: Carteira ("tem-um") [Agregação]
│  └─ carteiraId, saldoDisponivel, ativos (Map), dataCriacao
│
└─ contas: List<Conta> ("tem-um") [Agregação]
   └─ banco, agencia, numeroConta, saldo

OrdemInvestimento
└─ ordemId, tipo (COMPRA/VENDA), ticker, quantidade, 
   precoUnitario, precoTotal, status, dataCriacao
```

---

## 🔌 Protocolo RMI sobre UDP

### Estrutura de Requisição (RMIRequest)

```
┌──────────────────────────────────────────────┐
│  messageType: int = 0 (Requisição)           │
│  requestId: int (ID único da requisição)    │
│  objectReference: String (nome do serviço)  │
│  methodId: String (nome do método)          │
│  arguments: byte[] (argumentos serializados)│
└──────────────────────────────────────────────┘
```

### Estrutura de Resposta (RMIReply)

```
┌──────────────────────────────────────────────┐
│  messageType: int = 1 (Resposta)             │
│  requestId: int (ID da requisição original) │
│  resultBytes: byte[] (resultado em Protobuf)│
└──────────────────────────────────────────────┘
```

### RMICommunication - Três Métodos Principais

#### 1. doOperation() - Cliente
Envia requisição e aguarda resposta com timeout:
```java
byte[] response = communication.doOperation(
    remoteRef,           // RemoteObjectRef (host, port)
    "criarInvestidor",   // methodId
    argumentsBytes       // Serializado com DataOutputStream
);
```

#### 2. getRequest() - Servidor
Aguarda requisição de cliente:
```java
RMIRequest req = communication.getRequest();
// Processa: req.getMethodId(), req.getArguments()
```

#### 3. sendReply() - Servidor
Envia resposta de volta ao cliente:
```java
communication.sendReply(responseBytes, clientHost, clientPort);
```

---

## 📦 Componentes Principais

### RMIClient.java
Cliente interativo que invoca métodos remotos:
```java
RMIClient client = new RMIClient("localhost", 5000);

// Método 1: criarInvestidor (argumentos primitivos)
Investidor inv = client.criarInvestidor("inv1", "João", "123.456.789-00", 
                                         "joao@email.com", "(11)99999-0000");

// Método 2: obterInvestidor
Investidor inv2 = client.obterInvestidor("inv1");

// Método 3: criarOrdem
OrdemInvestimento ordem = client.criarOrdem("ord1", "COMPRA", "PETR4", 100, 25.50);

// Método 4: obterOrdensDoInvestidor (retorna array)
OrdemInvestimento[] ordens = client.obterOrdensDoInvestidor("inv1");

// Método 5: adicionarSaldoCarteira (retorna double)
double novoSaldo = client.adicionarSaldoCarteira("inv1", 5000.0);

// Método 6: obterAtivo (polimorfismo: AtivoB3 ou AtivoFixo)
Ativo ativo = client.obterAtivo("PETR4");
```

Menu interativo com 7 opções (6 remotas + sair).

### RMIServer.java
Servidor que despachador métodos remotos:
- Aguarda requisições UDP
- Deserializa argumentos com `DataInputStream`
- Invoca métodos em `InvestidorServiceImpl`
- Serializa respostas com `ProtobufSerializer`
- Envia respostas via UDP

### ProtobufSerializer.java
Converte entre modelos de domínio e Protocol Buffers:
```java
// Serialização (Modelo → Protobuf)
br.ufc.ds.trabalho2.rmi.pb.Investidor pbInv = 
    ProtobufSerializer.serializeInvestidor(modelInv);
byte[] bytes = pbInv.toByteArray();

// Desserialização (Protobuf → Modelo)
br.ufc.ds.trabalho2.rmi.pb.Investidor pbInv = 
    br.ufc.ds.trabalho2.rmi.pb.Investidor.parseFrom(bytes);
Investidor modelInv = ProtobufSerializer.deserializeInvestidor(pbInv);
```

Implementa 8 métodos (4 serialização + 4 desserialização).

### RemoteObjectRef.java
Referência remota que identifica o servidor:
```java
RemoteObjectRef ref = new RemoteObjectRef(
    "investidor_service",                    // objectReference
    InetAddress.getByName("localhost"),     // hostAddress
    5000                                     // port
);
```

---

## 🚀 Compilação e Execução

### Pré-requisitos
- Java 11+
- Maven 3.6+

### Compilação

```bash
cd trabalho2

# Compilar com Maven (gera classes Protobuf automaticamente)
mvn clean compile

# Ou usar script
./compilar.sh
```

### Execução

**Terminal 1 - Servidor:**
```bash
cd trabalho2

# Via Maven



# Ou via script
./servidor.sh
```

Saída esperada:
```
[RMIServer] Iniciando servidor RMI com UDP
[RMIServer] Host: localhost, Port: 5000
[RMIServer] Servidor RMI iniciado em UDP port 5000
[RMIServer] Aguardando requisições...
```

**Terminal 2 - Cliente:**
```bash
cd trabalho2

# Via Maven
mvn exec:java -Dexec.mainClass="br.ufc.ds.trabalho2.rmi.RMIClient" \
              -Dexec.args="localhost 5000"

# Ou via script
./cliente.sh
```

---

## 💻 Exemplo de Uso Interativo

```
[RMIClient] Conectado ao servidor em localhost:5000

========== Cliente RMI com UDP ==========
Digite o numero da opcao e forneca os dados conforme solicitado.

=== Menu RMI Cliente ===
1) criarInvestidor - criar novo investidor com dados pessoais
2) obterInvestidor - recuperar investidor existente pelo ID
3) criarOrdem - criar ordem de investimento para um ticker
4) obterOrdensDoInvestidor - listar ordens do investidor
5) adicionarSaldoCarteira - adicionar saldo na carteira
6) obterAtivo - consultar detalhes de um ativo
7) sair
Escolha uma opcao (1-7): 1

Criar um investidor exige um ID unico e dados pessoais.
investidorId (ex: inv1): inv1
nome completo: João Silva
cpf (ex: 000.000.000-00): 123.456.789-00
email: joao@email.com
telefone (ex: 99999-0000): (11)99999-0000

[RMIServer] Requisição recebida: criarInvestidor (requestId=52512)
[InvestidorServiceImpl] Investidor criado: inv1
[RMIServer] Resposta enviada para requestId=52512

Criado: Investidor{investidorId='inv1', nome='João Silva', cpf='123.456.789-00',
         carteira=Carteira{carteiraId='inv1_cart', ativos={}, saldoDisponivel=10000.0}}

=== Menu RMI Cliente ===
1) criarInvestidor - criar novo investidor com dados pessoais
...
Escolha uma opcao (1-7): 7
Saindo...
```

---

## 📁 Estrutura de Arquivos

```
trabalho2/
├── README.md                                 # Este arquivo
├── pom.xml                                   # Configuração Maven
├── servidor.sh                               # Script servidor
├── cliente.sh                                # Script cliente
├── compilar.sh                               # Script compilação
│
├── src/main/
│   ├── java/br/ufc/ds/trabalho2/
│   │   ├── app/
│   │   │   ├── InvestidorService.java        # Interface de serviço
│   │   │   ├── InvestidorServiceImpl.java     # Lógica de negócio
│   │   │   └── InvestidorServiceRemote.java  # Interface remota (legacy)
│   │   │
│   │   ├── model/
│   │   │   ├── Ativo.java                    # Classe abstrata
│   │   │   ├── AtivoB3.java                  # Subclasse (ações)
│   │   │   ├── AtivoFixo.java                # Subclasse (renda fixa)
│   │   │   ├── Investidor.java               # Entidade principal
│   │   │   ├── Carteira.java                 # Agregação
│   │   │   ├── Conta.java                    # Agregação
│   │   │   └── OrdemInvestimento.java        # Ordem de negociação
│   │   │
│   │   └── rmi/
│   │       ├── RMIClient.java                # Cliente RMI
│   │       ├── RMIServer.java                # Servidor RMI
│   │       ├── RMICommunication.java         # Comunicação UDP
│   │       ├── RMIRequest.java               # Requisição
│   │       ├── RMIReply.java                 # Resposta
│   │       ├── RemoteObjectRef.java          # Referência remota
│   │       ├── RemoteObjectManager.java      # Gerenciador
│   │       └── ProtobufSerializer.java       # Serializador Protobuf
│   │
│   └── proto/
│       ├── model.proto                       # Definição de modelos
│       └── rmi.proto                         # Definição de mensagens RMI
│
├── target/
│   ├── classes/                              # Classes compiladas
│   └── generated-sources/protobuf/java/      # Geradas do Protobuf
│
└── lib/ (se necessário)
    └── protobuf-java-*.jar
```

---

## 🔄 Fluxo de Execução

### 1. Cliente envia requisição:
```
RMIClient → DataOutputStream (serializa argumentos)
         → RMICommunication.doOperation()
         → UDP DatagramSocket (envia host:port)
```

### 2. Servidor recebe requisição:
```
RMIServer ← UDP DatagramSocket (recebe)
         ← RMICommunication.getRequest()
         ← Deserializa com DataInputStream
```

### 3. Servidor executa método:
```
RMIServer → Identifica methodId
         → Invoca InvestidorServiceImpl.metodo()
         → Serializa resultado com ProtobufSerializer
```

### 4. Servidor envia resposta:
```
RMIServer → RMICommunication.sendReply()
         → UDP DatagramSocket (envia host:port)
```

### 5. Cliente recebe resposta:
```
RMIClient ← UDP DatagramSocket (recebe)
         ← Deserializa com ProtobufSerializer
         ← Retorna objeto de domínio ao usuário
```

---

## 📊 Operações Remotas (6 Métodos)

| # | Método | Entrada | Saída | Tipo |
|---|--------|---------|-------|------|
| 1 | criarInvestidor | id, nome, cpf, email, tel | Investidor | Agregação |
| 2 | obterInvestidor | investidorId | Investidor | Referência |
| 3 | criarOrdem | id, tipo, ticker, qtd, preço | OrdemInvestimento | Valor |
| 4 | obterOrdensDoInvestidor | investidorId | OrdemInvestimento[] | Array |
| 5 | adicionarSaldoCarteira | investidorId, valor | double | Primitivo |
| 6 | obterAtivo | ticker | Ativo (polimórfico) | Extensão |

---

## ✨ Características Implementadas

✓ **Passagem por Valor**: argumentos serializados com DataOutputStream
✓ **Passagem por Referência**: RemoteObjectRef identifica servidor
✓ **Protocol Buffers**: serialização eficiente de respostas
✓ **UDP**: comunicação requisição-resposta sem TCP
✓ **Polimorfismo**: Ativo retorna AtivoB3 ou AtivoFixo
✓ **Arrays**: OrdemInvestimento[] via OrdensListaWrapper (Protobuf)
✓ **Primitivos**: adicionarSaldoCarteira retorna double
✓ **Tratamento de Erro**: exceções em cliente/servidor
✓ **Menu Interativo**: 7 opções no cliente (6 remotas + sair)
✓ **Logging**: mensagens de requisição/resposta

---

## 🛠️ Troubleshooting

### Porta já em uso
```
ERROR: Address already in use
```
Solução: Aguarde 30 segundos ou mude a porta no script.

### Compilação falha com Protobuf
```
ERROR: cannot find symbol class Investidor
```
Solução: Execute `mvn clean compile` para regenerar.

### Cliente não conecta ao servidor
```
ERROR: Connection timeout
```
Solução: Verifique se servidor está rodando na mesma porta/host.

---

## 📚 Referências

- [Java UDP Documentation](https://docs.oracle.com/javase/tutorial/networking/datagrams/)
- [Protocol Buffers Java](https://developers.google.com/protocol-buffers/docs/javatutorial)
- [Maven Guide](https://maven.apache.org/)
- [RMI Concepts](https://docs.oracle.com/javase/tutorial/rmi/)

---

## 📝 Notas

- Implementação 100% em UDP sem sockets TCP
- Suporte a múltiplos clientes sequenciais
- Serialização eficiente com Protocol Buffers
- Código bem documentado e estruturado

**Data**: Maio de 2026 | **Disciplina**: Sistemas Distribuídos - UFC