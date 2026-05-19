# Arquitetura de Rede: Cliente-Servidor Remoto

## Diagrama da Comunicação

```
┌─────────────────────────────────────────────────────────────┐
│                          INTERNET/LAN                        │
│                       (TCP porta 7070)                       │
└─────────────────────────────────────────────────────────────┘
            ▲                                    ▲
            │                                    │
            │ Resposta serializada              │ Requisição serializada
            │ (InvestmentReply em bytes)        │ (InvestmentRequest em bytes)
            │                                    │
            └────────────────────────────────────┘
            
┌──────────────────────────┐         ┌──────────────────────────┐
│   MÁQUINA SERVIDOR       │         │   MÁQUINA CLIENTE        │
│   (192.168.0.10)         │         │   (192.168.0.20)         │
│                          │         │                          │
│ ┌─────────────────────┐  │         │ ┌─────────────────────┐  │
│ │ InvestmentRpcServer │  │  ◄──────┼─│ InvestmentRpcClient │  │
│ │                     │  │         │ │                     │  │
│ │ Escutando porta 7070│  │         │ │ Interface interativa│  │
│ │                     │  │         │ │                     │  │
│ │ Multi-threaded:     │  │         │ │ Lê do teclado:      │  │
│ │ - Recebe requests   │  │  ──────►│ │  "COMPRA PETR4 50"  │  │
│ │ - Processa ordens   │  │         │ │                     │  │
│ │ - Envia replies     │  │         │ │ Serializa e envia   │  │
│ │                     │  │         │ │ Recebe resposta     │  │
│ │ Dados de market:    │  │         │ │ Desserializa        │  │
│ │ - PETR4: R$ 39,10   │  │         │ │ Exibe resultado     │  │
│ │ - VALE3: R$ 68,30   │  │         │ └─────────────────────┘  │
│ │ - ITUB4: R$ 31,40   │  │         │                          │
│ │ - BBDC4: R$ 13,00   │  │         │ Pode rodar múltiplas     │
│ │                     │  │         │ instâncias em paralelo   │
│ └─────────────────────┘  │         └──────────────────────────┘
│                          │
└──────────────────────────┘
```

## Fluxo de uma Ordem

```
1. Cliente digita: "COMPRA PETR4 50"
   │
   ├─► ClienteMain lê input
   │
   ├─► Cria InvestmentRequest
   │   {operation: "COMPRA", ticker: "PETR4", quantity: 50}
   │
   ├─► Serializa para bytes:
   │   [tamanho] + bytes da requisição
   │
   ├─► Envia via socket TCP para 192.168.0.10:7070
   │
   │   [Na máquina servidor]
   │
   ├─► InvestmentRpcServer recebe bytes
   │
   ├─► Desserializa InvestmentRequest
   │
   ├─► Processa em thread dedicada:
   │   - Busca preço de PETR4: R$ 39,10
   │   - Calcula total: 39,10 × 50 = R$ 1.955,00
   │
   ├─► Cria InvestmentReply
   │   {success: true, message: "Ordem COMPRA recebida para PETR4",
   │    unitPrice: 39.10, totalValue: 1955.00}
   │
   ├─► Serializa para bytes
   │   [tamanho] + bytes da resposta
   │
   ├─► Envia de volta ao cliente via TCP
   │
   │   [Na máquina cliente]
   │
   ├─► Cliente recebe bytes
   │
   ├─► Desserializa InvestmentReply
   │
   └─► Exibe: "InvestmentReply{success=true, message='Ordem COMPRA 
       recebida para PETR4', unitPrice=39.1, totalValue=1955.0}"
```

## Serialização de Dados

### InvestmentRequest (requisição)

```
Formato binário (via DataOutputStream):
┌─────────────────────────────────────┐
│ UTF String: operation  ("COMPRA")    │ ← writeUTF()
├─────────────────────────────────────┤
│ UTF String: ticker     ("PETR4")     │ ← writeUTF()
├─────────────────────────────────────┤
│ Long: quantity         (50)          │ ← writeLong()
└─────────────────────────────────────┘
```

### InvestmentReply (resposta)

```
Formato binário (via DataOutputStream):
┌─────────────────────────────────────┐
│ Boolean: success       (true)        │ ← writeBoolean()
├─────────────────────────────────────┤
│ UTF String: message    ("Ordem...")  │ ← writeUTF()
├─────────────────────────────────────┤
│ Double: unitPrice      (39.10)       │ ← writeDouble()
├─────────────────────────────────────┤
│ Double: totalValue     (1955.00)     │ ← writeDouble()
└─────────────────────────────────────┘
```

## Protocolo TCP de Alto Nível

```
1. Cliente conecta em TCP
2. Cliente → Servidor:
   [4 bytes: tamanho da requisição] + [bytes da requisição serializada]
3. Servidor processa
4. Servidor → Cliente:
   [4 bytes: tamanho da resposta] + [bytes da resposta serializada]
5. Desconecta
```

## Por que funciona em máquinas remotas?

✓ **Socket TCP** - Protocolo padrão da internet (trabalha em LAN ou WAN)
✓ **Serialização explícita** - Converte objetos ↔ bytes de forma portável
✓ **Multi-threaded** - Cada cliente recebe uma thread dedicada no servidor
✓ **Stateless** - Cada conexão é independente (não precisa manter estado)

## Requisitos de Rede

| Item | Servidor | Cliente |
|------|----------|---------|
| **IP** | Qualquer (escuta 0.0.0.0 na porta) | IP do servidor |
| **Porta TCP** | 7070 | 7070 (remota) |
| **Firewall** | Abrir porta 7070 | Sem requerimentos |
| **Latência** | N/A | Baixa latência recomendada |
| **Banda** | Mínima (bytes/requisição) | Mínima |

## Escalabilidade

O servidor **suporta múltiplos clientes simultâneos**:

```
Servidor (thread pool implícito via AcceptLoop):
- Thread principal: aceita conexões em loop
- Para cada conexão: cria nova thread para handleClient()
- Responde a múltiplos clientes em paralelo

Limite prático:
- ~1000 conexões simultâneas em JVM padrão
- CPU/memória é o gargalo, não TCP
```

Exemplo com 3 clientes ao mesmo tempo:

```
Servidor:
 Thread 1: conecta Cliente A, processa COMPRA PETR4
 Thread 2: conecta Cliente B, processa VENDA VALE3
 Thread 3: conecta Cliente C, processa COMPRA ITUB4
 (todas rodando em paralelo)
```
