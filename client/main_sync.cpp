
#include <iostream>

#include <grpc++/grpc++.h>
#include "ProfileCreateClient.cpp"

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;

int main(int argc, char **argv) {
    ProfileCreateClient creator(
            grpc::CreateChannel("localhost:50051", grpc::InsecureCredentials()));
    CreateResponseDto reply = creator.Create();
    std::cout << "Received: " << reply.info().id() << " secret: " << reply.secret() << std::endl;

    return 0;
}