package net.crazyproger.borsch.helloworld

import io.grpc.ManagedChannelBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private class HelloWorldClient(val host: String, val port: Int) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(HelloWorldClient::class.java)
    }

    val channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext(true).build()

    val blockingStub = GreeterGrpc.newBlockingStub(channel);

    fun shutdown() = channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)

    fun greet(name: String) {
        try {
            log.info("Will try to greet $name ...");
            val request = HelloRequest.newBuilder().setName(name).build();
            val response = blockingStub.sayHello(request);
            log.info("Greeting: {}", response.message);
        } catch (e: RuntimeException) {
            log.warn("RPC failed", e);
        }
    }
}

fun main(args: Array<String>) {
    val client = HelloWorldClient("localhost", 50051)
    try {
        val user = if (args.isEmpty()) "world" else args[0]
        client.greet(user)
    } finally {
        client.shutdown()
    }
}