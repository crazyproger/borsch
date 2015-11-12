package net.crazyproger.borsch.test

import io.grpc.*
import net.crazyproger.borsch.rpc.IdentificationInterceptor

class ClientIdentificationInterceptor(val secret: String) : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(method: MethodDescriptor<ReqT, RespT>, callOptions: CallOptions?, next: Channel): ClientCall<ReqT, RespT>? {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>?, headers: Metadata) {
                headers.put(IdentificationInterceptor.SECRET_ID_KEY, secret)
                delegate().start(responseListener, headers)
            }
        }
    }
}