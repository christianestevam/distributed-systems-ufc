#!/bin/bash

# Script para compilar Trabalho 2 com Maven

echo "=========================================="
echo "Compilando Trabalho 2 com Protocol Buffers"
echo "=========================================="
echo ""

# Verificar se Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "✗ Maven não encontrado. Instalando..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        brew install maven
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        sudo apt-get install maven -y
    else
        echo "Instale Maven manualmente de: https://maven.apache.org/download.cgi"
        exit 1
    fi
fi

# Limpar build anterior
echo "[1/2] Limpando build anterior..."
mvn clean -q
if [ $? -ne 0 ]; then
    echo "✗ Erro ao limpar"
    exit 1
fi
echo "✓ Build anterior removido"
echo ""

# Compilar código Java (Maven gera Protobuf automaticamente)
echo "[2/2] Compilando arquivos Java e Protocol Buffers..."
mvn compile -q
if [ $? -eq 0 ]; then
    echo "✓ Compilação bem-sucedida!"
    echo ""
    echo "=========================================="
    echo "Próximos passos:"
    echo "=========================================="
    echo ""
    echo "Terminal 1 - Servidor:"
    echo "  mvn exec:java -Dexec.mainClass=\"br.ufc.ds.trabalho2.rmi.RMIServer\" -Dexec.args=\"localhost 5000\""
    echo ""
    echo "Terminal 2 - Cliente:"
    echo "  mvn exec:java -Dexec.mainClass=\"br.ufc.ds.trabalho2.rmi.RMIClient\" -Dexec.args=\"localhost 5000\""
    echo ""
else
    echo "✗ Erro na compilação Java"
    exit 1
fi
echo "=========================================="
