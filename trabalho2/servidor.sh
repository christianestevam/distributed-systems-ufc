#!/bin/bash

# Script para executar o servidor RMI com Protocol Buffers

if [ ! -d "out" ]; then
    echo "Compilando projeto..."
    ./compilar.sh
fi

PORT=${1:-9999}

# Incluir protobuf na classpath
CLASSPATH="lib/protobuf-java-3.21.1.jar:out:."

echo "Iniciando servidor RMI na porta $PORT..."
java -cp $CLASSPATH br.ufc.ds.trabalho2.rmi.RMIServer $PORT
