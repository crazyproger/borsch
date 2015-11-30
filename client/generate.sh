#!/bin/bash

# 1. install protoc with brew "curl -fsSL https://goo.gl/getgrpc | bash -"


PROTO_PATH=../src/main/proto/

protoc --proto_path=$PROTO_PATH --cpp_out=generated *.proto
protoc --proto_path=$PROTO_PATH --grpc_out=generated *.proto

# 2. run cmake with -DCMAKE_CXX_COMPILER=g++
