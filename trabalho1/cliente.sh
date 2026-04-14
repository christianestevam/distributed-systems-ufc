#!/bin/bash

# Cliente sem Maven (usa javac + java direto)

echo "=========================================="
echo "Cliente de Investimentos B3"
echo "=========================================="
echo ""

# Usar argumentos ou pedir ao usuário
if [ -z "$1" ]; then
    echo "Digite o IP do servidor (exemplo: 192.168.0.10):"
    read IP_INPUT
    IP_SERVIDOR=$IP_INPUT
else
    IP_SERVIDOR=$1
fi

# Remover porta se o usuário digitar junto
IP_SERVIDOR=$(echo "$IP_SERVIDOR" | cut -d':' -f1)

PORTA=${2:-7070}

echo ""
echo "Conectando em: $IP_SERVIDOR:$PORTA"
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

echo "Iniciando cliente..."
echo ""

# Executar cliente direto com java
java -cp out br.ufc.ds.trabalho1.app.ClienteMain "$IP_SERVIDOR" "$PORTA"
