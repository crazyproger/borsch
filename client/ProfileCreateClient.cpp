//#ifndef ProfileCreateClient
//#define ProfileCreateClient

#include <iostream>

#include <grpc++/grpc++.h>
#include <google/protobuf/empty.pb.h>
#include "generated/player.grpc.pb.h"

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;
using borsch::ProfileCreateService;
using borsch::CreateResponseDto;
using google::protobuf::Empty;


class ProfileCreateClient {
public:
    ProfileCreateClient(std::shared_ptr<Channel> channel)
            : stub_(ProfileCreateService::NewStub(channel)) { }

    const ::borsch::CreateResponseDto Create() {
        Empty request;
//        request.set_name(user);

        // Container for the data we expect from the server.
        CreateResponseDto reply;

        // Context for the client. It could be used to convey extra information to
        // the server and/or tweak certain RPC behaviors.
        ClientContext context;

        // The actual RPC.
        Status status = stub_->Create(&context, request, &reply);

        // Act upon its status.
        if (status.ok()) {
            return reply;
        } else {
            throw 1;
        }
    }

private:
    std::unique_ptr<ProfileCreateService::Stub> stub_;
};

//#endif