# Guia: Executar Servidor e Cliente em 2 Máquinas Remotas

## Pré-requisitos

- Java 17+ instalado em ambas as máquinas
- Maven instalado (ou compilar com javac)
- Ambas as máquinas na mesma rede (ou com acesso de rede remota)
- Porta TCP 7070 aberta no firewall do servidor

## Passo 1: Descobrir o IP da Máquina Servidora

### No Linux/Mac (Servidor):

```bash
# Listar todos os IPs locais
ifconfig

# Ou mais simples:
hostname -I
```

### No Windows (Servidor):

```bash
ipconfig
```

### Exemplo de saída:
```
192.168.0.10  (rede local)
ou
203.0.113.45  (IP público, se for remoto via internet)
```

**Guarde este IP** - você vai precisar no cliente.

## Passo 2: Compilar o Projeto (em ambas as máquinas)

Em cada máquina, navegue até a pasta do projeto:

```bash
cd /caminho/para/trabalho1
```

Compile com Maven:

```bash
mvn clean compile
```

Ou com javac (se Maven não estiver disponível):

```bash
mkdir -p out
find src/main/java -name "*.java" | xargs javac -d out
```

## Passo 3: Executar o Servidor (Máquina 1)

Na máquina **servidor**, execute:

```bash
mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ServidorMain -Dexec.args="7070"
```

Será exibido:
```
InvestmentRpcServer ouvindo em 7070
```

**O servidor fica ouvindo indefinidamente.**

## Passo 4: Executar o Cliente (Máquina 2)

Na máquina **cliente**, execute (substituindo `IP_DO_SERVIDOR` pelo IP descoberto no Passo 1):

```bash
mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ClienteMain -Dexec.args="IP_DO_SERVIDOR 7070"
```

### Exemplos:

**Rede local (mesmo switch/roteador):**
```bash
mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ClienteMain -Dexec.args="192.168.0.10 7070"
```

**Acesso remoto via internet:**
```bash
mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ClienteMain -Dexec.args="203.0.113.45 7070"
```

**Máquinas no mesmo computador (teste local):**
```bash
mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ClienteMain -Dexec.args="127.0.0.1 7070"
```

## Passo 5: Usar o Cliente

Após conectar, você verá:

```
Cliente de Investimentos conectado com B3 (simulado)
Servidor: 192.168.0.10:7070
Digite: COMPRA PETR4 10 | VENDA VALE3 5 | SAIR
>
```

Digite ordens no formato: `OPERACAO TICKER QUANTIDADE`

### Exemplos de comandos:

```
> COMPRA PETR4 100
InvestmentReply{success=true, message='Ordem COMPRA recebida para PETR4', unitPrice=39.1, totalValue=3910.0}

> VENDA VALE3 50
InvestmentReply{success=true, message='Ordem VENDA recebida para VALE3', unitPrice=68.3, totalValue=3415.0}

> SAIR
```

Tickers disponíveis (simulados):
- `PETR4` - R$ 39,10
- `VALE3` - R$ 68,30
- `ITUB4` - R$ 31,40
- `BBDC4` - R$ 13,00

## Troubleshooting

### "Connection refused" ou "Connection timed out"

1. Verifique se o servidor está rodando na máquina 1
2. Confirme que o IP está correto (rode `ping IP_DO_SERVIDOR`)
3. Verify port com: `netstat -tuln | grep 7070` (Linux) ou `netstat -ano | findstr 7070` (Windows)
4. Abra o firewall para porta 7070:

**Linux:**
```bash
sudo ufw allow 7070/tcp
```

**Windows (como Administrador):**
```bash
netsh advfirewall firewall add rule name="Investimento B3" dir=in action=allow protocol=tcp localport=7070
```

### "java: command not found"

Instale Java 17 ou superior.

### Outro cliente quer se conectar

Basta rodar o ClienteMain novamente em outra máquina, apontando para o mesmo servidor.

O servidor é **multi-threaded** e aceita múltiplas conexões paralelas.

## Testando localmente (sem 2 computadores)

Terminal 1 (Servidor):
```bash
mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ServidorMain -Dexec.args="7070"
```

Terminal 2 (Cliente 1):
```bash
mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ClienteMain -Dexec.args="127.0.0.1 7070"
```

Terminal 3 (Cliente 2 - conecta ao mesmo servidor):
```bash
mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ClienteMain -Dexec.args="127.0.0.1 7070"
```

Cada cliente pode enviar ordens simultaneamente!

## Opção alternativa: usar gateway HTTP→TCP com Cloudflare quick tunnel

Se não for possível expor a porta TCP diretamente (por NATs/filtragem), você pode expor um endpoint HTTPS público que encaminha para seu servidor via um gateway local.

Passos resumidos:

1. Compile sem Maven (opcional):

```bash
./compilar.sh
```

2. Inicie o gateway HTTP→TCP no servidor (porta local 8080 -> servidor TCP 7070):

```bash
java -cp out br.ufc.ds.trabalho1.gateway.HttpToTcpGateway 8080 localhost 7070
```

3. Crie um quick tunnel HTTP com `cloudflared`:

```bash
cloudflared tunnel --url http://localhost:8080
```

4. O `cloudflared` retornará um host HTTPS do tipo `https://<algo>.trycloudflare.com`.

5. Do cliente remoto, envie ordens via `curl` (ou via um cliente HTTP):

```bash
curl -X POST 'https://<SEU_HOST>.trycloudflare.com/order' \
	-H 'Content-Type: application/json' \
	-d '{"operation":"COMPRA","ticker":"PETR4","quantity":10}'
```

6. O gateway converte o JSON para o protocol binário e encaminha para `localhost:7070`, devolvendo o resultado em JSON.

Observação: essa opção permite uso de endpoints HTTPS públicos sem abrir a porta 7070 diretamente no firewall. Se preferir o cliente TCP original sem mudanças, use `cloudflared` com `tcp://` (opção A no README).
