#!/bin/bash

# Servidor sem Maven (usa javac + java direto)

echo "=========================================="
echo "Servidor de Investimentos B3"
echo "=========================================="
echo ""

PORTA=${1:-7070}

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

# Verificar se foi compilado
if [ ! -d "out" ]; then
    echo "Compilando projeto..."
    mkdir -p out
    find src/main/java -name "*.java" | xargs javac -d out
    if [ $? -ne 0 ]; then
        echo "Erro na compilação!"
        exit 1
    fi
fi

echo "Iniciando servidor..."
echo ""

# Executar servidor direto com java
java -cp out br.ufc.ds.trabalho1.app.ServidorMain "$PORTA"
