#!/bin/bash

# Script pentru generarea codului Python din fisierul .proto


echo "Generare cod Python din idm.proto..."

#compilare
python3 -m grpc_tools.protoc \
    -I./proto \
    --python_out=./src \
    --grpc_python_out=./src \
    ./proto/idm.proto

#verificare
if [ $? -eq 0 ]; then
    echo " Cod generat cu succes in directorul src/"
    echo "   - idm_pb2.py (mesajele)"
    echo "   - idm_pb2_grpc.py (serviciul)"
else
    echo "Eroare la generarea codului!"
    exit 1
fi