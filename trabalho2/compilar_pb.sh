#!/bin/bash

# Script para compilar Protocol Buffers e Java

echo "=========================================="
echo "Compilando Protocol Buffers e Projeto..."
echo "=========================================="
echo ""

# Verificar se protoc está instalado
if ! command -v protoc &> /dev/null; then
    echo "✗ protoc não encontrado. Instalando..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        brew install protobuf
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        sudo apt-get install protobuf-compiler -y
    else
        echo "Instale protobuf manualmente de: https://github.com/protocolbuffers/protobuf/releases"
        exit 1
    fi
fi

# Criar diretórios
mkdir -p out
mkdir -p src/main/java/br/ufc/ds/trabalho2/protobuf

# Compilar Protocol Buffers
echo "[1/3] Compilando arquivos .proto..."
protoc --java_out=src/main/java \
       --proto_path=src/main/proto \
       src/main/proto/rmi.proto \
       src/main/proto/model.proto

if [ $? -ne 0 ]; then
    echo "✗ Erro ao compilar Protocol Buffers"
    exit 1
fi
echo "✓ Protocol Buffers compilados"
echo ""

# Baixar libprotobuf (se necessário)
echo "[2/3] Verificando dependências Java..."
if [ ! -f "lib/protobuf-java-3.21.1.jar" ]; then
    mkdir -p lib
    echo "  Baixando protobuf-java..."
    curl -s -L https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/3.21.1/protobuf-java-3.21.1.jar \
         -o lib/protobuf-java-3.21.1.jar
fi
echo "✓ Dependências OK"
echo ""

# Compilar arquivos Java
echo "[3/3] Compilando arquivos .java..."
CLASSPATH="lib/protobuf-java-3.21.1.jar:."
find src/main/java -name "*.java" | xargs javac -cp $CLASSPATH -d out 2>&1

if [ $? -eq 0 ]; then
    echo "✓ Compilação bem-sucedida!"
    echo "Arquivos compilados em: out/"
else
    echo "✗ Erro na compilação Java"
    exit 1
fi
echo ""
echo "=========================================="
