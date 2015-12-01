//#ifndef ProfileCreateClientAsync
//#define ProfileCreateClientAsync

#include <iostream>

#include <grpc++/grpc++.h>
#include <google/protobuf/empty.pb.h>
#include "generated/player.grpc.pb.h"

using grpc::Channel;
using grpc::ClientAsyncResponseReader;
using grpc::ClientContext;
using grpc::Status;
using grpc::CompletionQueue;
using borsch::ProfileCreateService;
using borsch::CreateResponseDto;
using google::protobuf::Empty;
using namespace std::chrono;


class ProfileCreateClientAsync {
public:
    ProfileCreateClientAsync(std::shared_ptr<Channel> channel)
            : stub_(ProfileCreateService::NewStub(channel)) { }

    const ::borsch::CreateResponseDto Create() {
        Empty request;
//        request.set_name(user);

        // Container for the data we expect from the server.
        CreateResponseDto reply;

        // Context for the client. It could be used to convey extra information to
        // the server and/or tweak certain RPC behaviors.
        ClientContext context;

        // The producer-consumer queue we use to communicate asynchronously with the
        // gRPC runtime.
        CompletionQueue cq;

        // Storage for the status of the RPC upon completion.
        Status status;

        // stub_->AsyncSayHello() perform the RPC call, returning an instance we
        // store in "rpc". Because we are using the asynchronous API, we need the
        // hold on to the "rpc" instance in order to get updates on the ongoig RPC.
        std::unique_ptr<ClientAsyncResponseReader<CreateResponseDto> > rpc(
                stub_->AsyncCreate(&context, request, &cq));

        // Request that, upon completion of the RPC, "reply" be updated with the
        // server's response; "status" with the indication of whether the operation
        // was successful. Tag the request with the integer 1.
        rpc->Finish(&reply, &status, (void *) 1);

        void *got_tag;
        bool ok = false;
        // Block until the next result is available in the completion queue "cq".
        const gpr_timespec wait_time = gpr_time_from_nanos(10,  GPR_CLOCK_REALTIME);
        CompletionQueue::NextStatus nextStatus;
        do {
            std::printf("waiting\n");
            nextStatus = cq.AsyncNext(&got_tag, &ok, wait_time);
            std::printf("next status is %i\n", nextStatus);
        } while (nextStatus == CompletionQueue::NextStatus::TIMEOUT);



        // Verify that the result from "cq" corresponds, by its tag, our previous
        // request.
        GPR_ASSERT(got_tag == (void *) 1);
        // ... and that the request was completed successfully. Note that "ok"
        // corresponds solely to the request for updates introduced by Finish().
        GPR_ASSERT(ok);

        // Act upon the status of the actual RPC.
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