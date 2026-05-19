# Teste Prático Passo-a-Passo

## Cenário 1: Teste Local (Mesma máquina)

Útil para verificar se tudo funciona antes de usar 2 máquinas.

### Terminal 1: Servidor

```bash
cd /caminho/para/trabalho1

# Compilar uma única vez
mvn clean compile

# Rodar servidor
./iniciar-servidor.sh
```

Saída esperada:
```
==========================================
Servidor de Investimentos B3
==========================================

Seu IP local é: 127.0.0.1
Porta de escuta: 7070

Clientes devem conectar em: 127.0.0.1:7070

Iniciando servidor...

InvestmentRpcServer ouvindo em 7070
```

### Terminal 2: Cliente

```bash
cd /caminho/para/trabalho1

# Rodar cliente apontando para localhost
./iniciar-cliente.sh 127.0.0.1
```

Saída esperada:
```
==========================================
Cliente de Investimentos B3
==========================================

Conectando em: 127.0.0.1:7070

Testando conectividade...
✓ Servidor respondendo

Iniciando cliente...

Cliente de Investimentos conectado com B3 (simulado)
Servidor: 127.0.0.1:7070
Digite: COMPRA PETR4 10 | VENDA VALE3 5 | SAIR
>
```

### Testar comandos

Digite os comandos abaixo no cliente:

#### 1. Primeira ordem - Compra

```
> COMPRA PETR4 100
InvestmentReply{success=true, message='Ordem COMPRA recebida para PETR4', unitPrice=39.1, totalValue=3910.0}
```

✓ Esperado: `success=true`

#### 2. Segunda ordem - Venda

```
> VENDA VALE3 50
InvestmentReply{success=true, message='Ordem VENDA recebida para VALE3', unitPrice=68.3, totalValue=3415.0}
```

✓ Esperado: `success=true`

#### 3. Ticker inválido (teste erro)

```
> COMPRA INEXISTENTE 10
InvestmentReply{success=false, message='Ticker nao encontrado', unitPrice=0.0, totalValue=0.0}
```

✓ Esperado: `success=false`

#### 4. Sair

```
> SAIR
```

✓ Esperado: Cliente fecha

---

## Cenário 2: 2 Máquinas Reais

### Pré-requisito: Ambas na mesma rede

Verifique que as máquinas conseguem se comunicar:

```bash
# Na máquina Cliente, testar ping no servidor
ping 192.168.0.10
```

Esperado:
```
PING 192.168.0.10 (192.168.0.10): 56 data bytes
64 bytes from 192.168.0.10: icmp_seq=0 ttl=64 time=2.345 ms
```

### Máquina 1 (Servidor)

Descobrir IP:
```bash
hostname -I
# Saída: 192.168.0.10
```

Abrir firewall (Linux):
```bash
sudo ufw allow 7070/tcp
```

Ou Windows (Admin):
```bash
netsh advfirewall firewall add rule name="B3" dir=in action=allow protocol=tcp localport=7070
```

Compilar:
```bash
cd trabalho1
mvn clean compile
```

Rodar servidor:
```bash
./iniciar-servidor.sh
```

✓ Deixe rodando neste terminal

### Máquina 2 (Cliente)

Compilar:
```bash
cd trabalho1
mvn clean compile
```

Conectar ao servidor:
```bash
./iniciar-cliente.sh 192.168.0.10
```

Se bem-sucedido:
```
Conectando em: 192.168.0.10:7070
✓ Servidor respondendo
...
>
```

Testar ordens:
```
> COMPRA PETR4 100
InvestmentReply{...}

> VENDA VALE3 50
InvestmentReply{...}

> SAIR

### Alternativa: testar via Cloudflare quick tunnel + gateway HTTP→TCP

Se não for possível conexão direta por TCP, siga estes passos no servidor:

```bash
./compilar.sh
java -cp out br.ufc.ds.trabalho1.gateway.HttpToTcpGateway 8080 localhost 7070
cloudflared tunnel --url http://localhost:8080
```

Em seguida, no cliente remoto ou em qualquer lugar com acesso à internet use `curl`:

```bash
curl -X POST 'https://<SEU_HOST>.trycloudflare.com/order' \
    -H 'Content-Type: application/json' \
    -d '{"operation":"COMPRA","ticker":"PETR4","quantity":10}'
```

O gateway retornará um JSON com o resultado da ordem.
```

---

## Cenário 3: Múltiplos Clientes

Na mesma máquina servidor, abra **3 terminais de cliente**:

### Terminal Cliente 1
```bash
./iniciar-cliente.sh 192.168.0.10
```

### Terminal Cliente 2
```bash
./iniciar-cliente.sh 192.168.0.10
```

### Terminal Cliente 3
```bash
./iniciar-cliente.sh 192.168.0.10
```

Envie ordens em cada cliente:

**Cliente 1:**
```
> COMPRA PETR4 50
InvestmentReply{...success=true...}
```

**Cliente 2:**
```
> VENDA VALE3 30
InvestmentReply{...success=true...}
```

**Cliente 3:**
```
> COMPRA ITUB4 100
InvestmentReply{...success=true...}
```

✓ Todos conseguem se conectar e enviar ordens **simultaneamente**

---

## Troubleshooting durante os testes

| Problema | Causa | Solução |
|----------|-------|---------|
| `Connection refused` | Servidor não está rodando | Verifique Terminal 1 do servidor |
| `Connection timed out` | IP errado ou firewall bloqueando | Teste com `ping` primeiro |
| `java: command not found` | Java não instalado | Instale Java 17+ |
| `mvn not found` | Maven não instalado | Compile com `javac` ou instale Maven |
| Servidor recebe ordem mas não responde | Erro de serialização | Verifique versão Java (use 17+) |
| Cliente conecta mas cursor fica parado | Socket timeout | Verifique se servidor está respondendo |

---

## Verificar Porta em Uso

Se tiver erro "Address already in use port 7070":

```bash
# Linux/Mac: ver quem está usando porta 7070
lsof -i :7070

# Windows: 
netstat -ano | findstr 7070

# Matar processo (perigoso! verifique PID primeiro)
kill -9 <PID>          # Linux/Mac
taskkill /PID <PID> /F # Windows
```

---

## Teste de Carga (Opcional)

Rodar script que simula múltiplas compras:

```bash
#!/bin/bash
for i in {1..10}; do
    echo "Ordem $i..."
    echo "COMPRA PETR4 10" | ./iniciar-cliente.sh 192.168.0.10
    sleep 0.5
done
```

✓ Servidor deve processar todas sem erro.
