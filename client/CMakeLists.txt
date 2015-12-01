cmake_minimum_required(VERSION 3.3)
project(grpc_client)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

file(GLOB _gen_cc "generated/*.cc")
file(GLOB _gen_h "generated/*.h")

#include_directories(generated)

set(SOURCE_FILES
        ${SOURCE_FILES}
        ${_gen_cc}
        ${_gen_h}
        )
set(SOURCE_FILES_SYNC
        ${SOURCE_FILES}
        main_sync.cpp ProfileCreateClient.cpp)
set(SOURCE_FILES_ASYNC
        ${SOURCE_FILES}
        main_async.cpp ProfileCreateClientAsync.cpp)
set(grpc_client_VERSION_MAJOR 1)
set(grpc_client_VERSION_MINOR 0)

link_directories(/usr/local/lib)
find_library(GRPC_UNSECURE grpc++_unsecure)
find_library(GRPC grpc)
find_library(GPR gpr)
find_library(PROTOBUF protobuf)
find_library(PTHREAD pthread)
find_library(DL dl)


add_executable(grpc_client ${SOURCE_FILES_SYNC})
add_executable(grpc_client_async ${SOURCE_FILES_ASYNC})

target_link_libraries(grpc_client
        ${GRPC_UNSECURE}
        ${GRPC}
        ${GPR}
        ${PROTOBUF}
        ${PTHREAD}
        ${DL}
        )
target_link_libraries(grpc_client_async
        ${GRPC_UNSECURE}
        ${GRPC}
        ${GPR}
        ${PROTOBUF}
        ${PTHREAD}
        ${DL}
        )

