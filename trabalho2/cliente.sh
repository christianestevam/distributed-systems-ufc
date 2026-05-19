#!/bin/bash

# Script para executar o cliente RMI com Protocol Buffers

if [ ! -d "out" ]; then
    echo "Compilando projeto..."
    ./compilar.sh
fi

HOST=${1:-localhost}
PORT=${2:-9999}

# Incluir protobuf na classpath
CLASSPATH="lib/protobuf-java-3.21.1.jar:out:."

echo "Iniciando cliente RMI..."
java -cp $CLASSPATH br.ufc.ds.trabalho2.rmi.RMIClient $HOST $PORT
