#!/bin/bash

# Script para conectar cliente ao servidor de investimentos B3

echo "=========================================="
echo "Cliente de Investimentos B3"
echo "=========================================="
echo ""

# Usar argumentos ou pedir ao usuário
if [ -z "$1" ]; then
    echo "Digite o host do servidor (exemplo: 192.168.0.10 ou https://exemplo.trycloudflare.com):"
    read INPUT_HOST
else
    INPUT_HOST=$1
fi

# Extrai hostname e opcionalmente porta de uma URL completa (remove esquema e path)
# Exemplos de entrada aceitáveis:
# 192.168.0.6:7070
# https://fathers-enlarge-pipes-regional.trycloudflare.com
# tcp://example.trycloudflare.com:12345

RAW_HOST=$(echo "$INPUT_HOST" | sed -E 's#^.*://##' | sed -E 's#/.*$##')

HOST_ONLY=$(echo "$RAW_HOST" | cut -d':' -f1)
POSSIBLE_PORT=$(echo "$RAW_HOST" | awk -F: '{print $2}')

if [ -n "$POSSIBLE_PORT" ]; then
    IP_SERVIDOR=$HOST_ONLY
    PORTA=$POSSIBLE_PORT
else
    IP_SERVIDOR=$HOST_ONLY
    PORTA=${2:-7070}
fi

echo ""
echo "Conectando em: $IP_SERVIDOR:$PORTA"
echo ""

# Testar conectividade (ping)
echo "Testando conectividade..."
if ping -c 1 -W 2 "$IP_SERVIDOR" &> /dev/null; then
    echo "✓ Servidor respondendo"
else
    echo "✗ Aviso: servidor não responde ao ping (firewall pode estar bloqueando)"
fi

echo ""
echo "Iniciando cliente..."
echo ""

mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ClienteMain -Dexec.args="$IP_SERVIDOR $PORTA"
