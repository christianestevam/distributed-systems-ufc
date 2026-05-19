#!/bin/bash

# Script para compilar projeto SEM MAVEN (usando javac)

echo "Compilando projeto..."
echo ""

# Criar diretório de saída
mkdir -p out

# Compilar todos os .java em out/
find src/main/java -name "*.java" | xargs javac -d out 2>&1

if [ $? -eq 0 ]; then
    echo "✓ Compilação bem-sucedida!"
    echo "Arquivos compilados em: out/"
else
    echo "✗ Erro na compilação"
    exit 1
fi
