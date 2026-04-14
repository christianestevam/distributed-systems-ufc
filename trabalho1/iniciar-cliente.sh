#!/bin/bash

# Script para conectar cliente ao servidor de investimentos B3

echo "=========================================="
echo "Cliente de Investimentos B3"
echo "=========================================="
echo ""

# Usar argumentos ou pedir ao usuário
if [ -z "$1" ]; then
    echo "Digite o IP do servidor:"
    read IP_SERVIDOR
else
    IP_SERVIDOR=$1
fi

PORTA=${2:-7070}

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
