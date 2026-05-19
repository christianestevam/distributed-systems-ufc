# Início Rápido: 2 Máquinas Remotas

## Resumo em 4 passos

### 1. Máquina Servidor: Descobrir IP

```bash
# Linux/Mac
hostname -I

# Windows
ipconfig
```

Guarde o IP (ex: `192.168.0.10`)

### 2. Máquina Servidor: Compilar

```bash
cd trabalho1
mvn clean compile
```

### 3. Máquina Servidor: Rodar

**Linux/Mac:**
```bash
./iniciar-servidor.sh
```

**Windows:**
```bash
iniciar-servidor.bat
```

Saída esperada:
```
InvestmentRpcServer ouvindo em 7070
```

### 4. Máquina Cliente: Compilar e Rodar

```bash
cd trabalho1
mvn clean compile
```

**Linux/Mac:**
```bash
./iniciar-cliente.sh 192.168.0.10
```

**Windows:**
```bash
iniciar-cliente.bat 192.168.0.10
```

### 5. Cliente: Enviar Ordens

```
COMPRA PETR4 100
VENDA VALE3 50
SAIR
```

---

## Tickers disponíveis

- **PETR4** - R$ 39,10
- **VALE3** - R$ 68,30
- **ITUB4** - R$ 31,40
- **BBDC4** - R$ 13,00

---

## Firewall: Abrir porta 7070

**Linux:**
```bash
sudo ufw allow 7070/tcp
```

**Windows (como Admin):**
```bash
netsh advfirewall firewall add rule name="B3" dir=in action=allow protocol=tcp localport=7070
```

---

## Testar Conectividade

Antes de rodar o cliente:

```bash
ping IP_DO_SERVIDOR
```

Se funcionar = ✓ Pode prosseguir
Se não funcionar = Verifique firewall ou IP

---

## Múltiplos Clientes

Abra múltiplas janelas de terminal e rode o cliente em cada uma:

```bash
./iniciar-cliente.sh 192.168.0.10
./iniciar-cliente.sh 192.168.0.10  # outra janela
./iniciar-cliente.sh 192.168.0.10  # outra janela
```

Todos conectam ao mesmo servidor simultaneamente!

## Rápido: usar Cloudflare quick tunnel (se estiver em redes diferentes)

1. No servidor, compile e inicie o gateway HTTP→TCP:

```bash
./compilar.sh
java -cp out br.ufc.ds.trabalho1.gateway.HttpToTcpGateway 8080 localhost 7070
```

2. Em outro terminal do servidor, exponha com `cloudflared`:

```bash
cloudflared tunnel --url http://localhost:8080
```

3. Do cliente remoto, envie uma ordem via `curl` para o host HTTPS gerado:

```bash
curl -X POST 'https://<SEU_HOST>.trycloudflare.com/order' -H 'Content-Type: application/json' -d '{"operation":"COMPRA","ticker":"PETR4","quantity":10}'
```

Observação: essa alternativa permite testes rápidos sem abrir a porta 7070 no firewall.
