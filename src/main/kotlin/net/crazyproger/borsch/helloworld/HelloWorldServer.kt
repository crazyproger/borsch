package net.crazyproger.borsch.helloworld

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import kotlin.concurrent.thread

private class HelloWorldServer {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(HelloWorldServer::class.java)
    }

    val port = 50051
    var server: Server? = null

    fun start() {
        server = ServerBuilder.forPort(port)
                .addService(GreeterGrpc.bindService { request, observer -> sayHello(request, observer) })
                .build().start()
        log.info("Server started, listening on " + port)
        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            println("*** shutting down gRPC server since JVM is shutting down");
            stop();
            System.err.println("*** server shut down");
        });
    }

    fun stop() = server?.shutdown()

    fun blockUntilShutdown() = server?.awaitTermination()

    fun sayHello(request: HelloRequest, responseObserver: StreamObserver<HelloResponse>) {
        val reply = HelloResponse.newBuilder().setMessage("Hello " + request.name).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}

fun main(args: Array<String>) {
    val server = HelloWorldServer()
    server.start()
    server.blockUntilShutdown()
}
