# 📋 Projeto: Sistema de Investimentos B3 - Distribuído

**Versão**: 1.0  
**Data**: 13 de abril de 2026  
**Linguagem**: Java 17+  
**Arquitetura**: Cliente-Servidor RPC com Serialização TCP

---

## 📚 Documentação - Onde começar?

### Para implementação rápida (5 min)
👉 **[INICIO_RAPIDO.md](INICIO_RAPIDO.md)** - Passo-a-passo executar servidor e cliente

### Para entender a rede e arquitetura (10 min)
👉 **[ARQUITETURA_REDE.md](ARQUITETURA_REDE.md)** - Diagrama TCP, serialização, protocolo

### Para testes práticos e troubleshooting (15 min)
👉 **[TESTE_PRATICO.md](TESTE_PRATICO.md)** - Exemplos de teste local, 2 máquinas, múltiplos clientes

### Para configuração avançada (detalhes)
👉 **[GUIA_REMOTO.md](GUIA_REMOTO.md)** - Firewall, IP discovery, agregar múltiplos clientes

### Para resumo técnico (overview)
👉 **[README.md](README.md)** - Requisitos atendidos, estrutura de pacotes

---

## 🚀 Quick Start (Em 2 Minutos)

### Máquina 1 (Servidor):
```bash
cd trabalho1
mvn clean compile
./iniciar-servidor.sh
```

### Máquina 2 (Cliente):
```bash
cd trabalho1
mvn clean compile
./iniciar-cliente.sh 192.168.0.10  # trocar pelo IP do servidor
```

### Cliente (Digite):
```
COMPRA PETR4 100
VENDA VALE3 50
SAIR
```

---

## 📁 Estrutura do Projeto

```
trabalho1/
├── pom.xml                          # Configuração Maven
├── 
├── 📖 DOCUMENTAÇÃO
├── ├── INICIO_RAPIDO.md             ⭐ COMECE AQUI
├── ├── ARQUITETURA_REDE.md          (diagrama + protocolo)
├── ├── TESTE_PRATICO.md             (exemplos passo-a-passo)
├── ├── GUIA_REMOTO.md               (configuração avançada)
├── └── README.md                    (overview técnico)
│
├── 🚀 SCRIPTS
├── ├── iniciar-servidor.sh          (Linux/Mac)
├── ├── iniciar-servidor.bat         (Windows)
├── ├── iniciar-cliente.sh           (Linux/Mac)
├── └── iniciar-cliente.bat          (Windows)
│
├── 📦 CÓDIGO-FONTE (16 classes Java)
│
├── src/main/java/br/ufc/ds/trabalho1/
│   │
│   ├── app/
│   │   ├── ServidorMain.java        ← SERVIDOR (porta 7070)
│   │   └── ClienteMain.java         ← CLIENTE (interface interativa)
│   │
│   ├── investment/
│   │   ├── model/
│   │   │   ├── AtivoB3.java         (POJO: ativo da B3)
│   │   │   ├── Investidor.java      (POJO: investidor)
│   │   │   └── OrdemInvestimento.java (POJO: ordem de investimento)
│   │   │
│   │   ├── service/
│   │   │   ├── CarteiraService.java (gerencia posições)
│   │   │   └── OrdemService.java    (cria ordens validadas)
│   │   │
│   │   ├── stream/
│   │   │   ├── OrdemInvestimentoOutputStream.java (item 2)
│   │   │   └── OrdemInvestimentoInputStream.java  (item 3)
│   │   │
│   │   └── demo/
│   │       ├── StreamExerciseDemo.java      (testa stream)
│   │       ├── TcpStreamServer.java         (servidor stream)
│   │       └── TcpStreamClient.java         (cliente stream)
│   │
│   └── rpc/
│       ├── InvestmentRequest.java   (serializa requisição)
│       ├── InvestmentReply.java     (serializa resposta)
│       ├── InvestmentRpcServer.java (processador multithread)
│       └── InvestmentRpcClient.java (cliente RPC)
│
└── out/                             (compilados .class)
```

---

## ✅ Requisitos Atendidos (Trabalho 1 - Itens 1 a 4)

| Item | Status | Implementation | Arquivo |
|------|--------|-----------------|---------|
| 1. POJOs + Serviços | ✓ | `AtivoB3`, `Investidor`, `OrdemInvestimento`, `CarteiraService`, `OrdemService` | `investment/model/`, `investment/service/` |
| 2. OutputStream custom | ✓ | `OrdemInvestimentoOutputStream` com array, quantidade e destino | `investment/stream/OrdemInvestimentoOutputStream.java` |
| 3. InputStream custom | ✓ | `OrdemInvestimentoInputStream` lendo dados do OutputStream | `investment/stream/OrdemInvestimentoInputStream.java` |
| 4. RPC Serializado | ✓ | Request/Reply packet + MultiThread TCP Server | `rpc/` + `app/ServidorMain.java` |

---

## 🌐 Tickers Disponíveis (Simulados da B3)

| Ticker | Preço | Setor |
|--------|-------|-------|
| PETR4  | R$ 39,10 | Energia |
| VALE3  | R$ 68,30 | Mineração |
| ITUB4  | R$ 31,40 | Banco |
| BBDC4  | R$ 13,00 | Banco |

---

## 🔌 Protocolo de Comunicação

```
CLIENT → SERVER:
[4 bytes: tamanho] + [bytes: InvestmentRequest serializado]

SERVER → CLIENT:
[4 bytes: tamanho] + [bytes: InvestmentReply serializado]

Operações suportadas:
- COMPRA TICKER QUANTIDADE
- VENDA  TICKER QUANTIDADE
```

---

## 💻 Requisitos do Sistema

- **Java**: 17+
- **Maven**: 3.6+ (opcional - pode compilar com javac)
- **Rede**: TCP porta 7070 aberta no firewall do servidor
- **SO**: Linux, macOS ou Windows

---

## 🔐 Segurança & Escalabilidade

✓ **Multi-threaded**: Cada cliente recebe thread dedicada
✓ **Stateless**: Sem dependência de estado anterior
✓ **TCP confiável**: Entrega garantida de mensagens
✓ **Serialização rigorosa**: Sem serialização Java (compatível cross-platform)

**Limite prático**: ~1000 conexões simultâneas (CPU/memória é o gargalo)

---

## 📝 Workflows Comuns

### Workflow 1: Testar Localmente (uma máquina)

```bash
# Terminal 1
./iniciar-servidor.sh

# Terminal 2
./iniciar-cliente.sh 127.0.0.1
```

### Workflow 2: Rodar em 2 Máquinas

```
Máquina A (Servidor):  ./iniciar-servidor.sh
Máquina B (Cliente):   ./iniciar-cliente.sh <IP_A>
```

### Workflow 3: 3+ Clientes ao Mesmo Servidor

```bash
# Máquina A (servidor)
./iniciar-servidor.sh

# Máquinas B, C, D etc. (clientes)
./iniciar-cliente.sh <IP_A>
./iniciar-cliente.sh <IP_A>
./iniciar-cliente.sh <IP_A>
```

---

## 🐛 Troubleshooting Rápido

| Erro | Causa | Comando |
|------|-------|---------|
| `Connection refused` | Servidor não rodando | `./iniciar-servidor.sh` |
| `Connection timed out` | IP errado/firewall | `ping IP_DO_SERVIDOR` |
| `Port already in use` | Porta 7070 em uso | `lsof -i :7070` (Mac/Linux) |
| `mvn not found` | Maven não instalado | `mvn clean compile` ou `javac` |
| `java: command not found` | Java não instalado | Instale Java 17+ |

---

## 📧 Teste de Conectividade

```bash
# Testar se cliente consegue alcançar servidor
ping <IP_DO_SERVIDOR>

# Testar se porta 7070 está aberta no servidor
telnet <IP_DO_SERVIDOR> 7070  # Ctrl+] depois quit (Mac/Linux)

# Ver processo usando porta 7070
lsof -i :7070        # Mac/Linux
netstat -ano | grep 7070  # Windows
```

---

## 📖 Para Apresentação

1. **Demo Local** (2 min): Terminal 1 = servidor, Terminal 2 = cliente
   - Mostrar servidor recebendo conexão
   - Mostrar cliente enviando ordens
   - Mostrar resposta em tempo real

2. **Demo Remoto** (3 min): Dois computadores
   - Servidor em Notebook A
   - Cliente em Notebook B
   - Enviar ordens de B para A

3. **Código Architecture** (5 min):
   - Mostrar `InvestmentRequest` → serialização
   - Mostrar `InvestmentRpcServer` → thread handling
   - Mostrar `InvestmentReply` → deserialização

---

**Última atualização**: 13 de abril de 2026  
**Desenvolvido para**: Trabalho 1 - Comunicação entre processos (UFC)
