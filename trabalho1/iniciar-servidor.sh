#!/bin/bash

# Script para iniciar o servidor de investimentos B3

PORTA=${1:-7070}

echo "=========================================="
echo "Servidor de Investimentos B3"
echo "=========================================="
echo ""

# Descobrir IP local
IP_LOCAL=$(hostname -I | awk '{print $1}')
if [ -z "$IP_LOCAL" ]; then
    IP_LOCAL=$(ifconfig | grep "inet " | grep -v "127.0.0.1" | awk '{print $2}' | head -1)
fi

echo "Seu IP local é: $IP_LOCAL"
echo "Porta de escuta: $PORTA"
echo ""
echo "Clientes devem conectar em: $IP_LOCAL:$PORTA"
echo ""
echo "Iniciando servidor..."
echo ""

mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ServidorMain -Dexec.args="$PORTA"
