package net.crazyproger.borsch.test

import io.grpc.*
import io.grpc.stub.AbstractStub
import net.crazyproger.borsch.rpc.BusinessErrors
import net.crazyproger.borsch.rpc.MetadataKeys
import net.crazyproger.borsch.rpc.toException

class ClientIdentificationInterceptor(val secret: String) : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(method: MethodDescriptor<ReqT, RespT>, callOptions: CallOptions?, next: Channel): ClientCall<ReqT, RespT>? {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>?, headers: Metadata) {
                headers.put(MetadataKeys.secretId, secret)
                delegate().start(responseListener, headers)
            }
        }
    }
}

fun <R, X : AbstractStub<X>> withBusinessError(stub: X, body: X.() -> R): R {
    var error: BusinessErrors.Error? = null
    val intercepted = stub.withInterceptors(ClientBusinessErrorInterceptor({ error = it }))
    try {
        return intercepted.run(body)
    } catch(e: StatusRuntimeException) {
        throw error?.toException() ?: e
    } catch(e: StatusException) {
        throw error?.toException() ?: e
    }
}

class ClientBusinessErrorInterceptor(val errorConsumer: (BusinessErrors.Error) -> Unit) : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(method: MethodDescriptor<ReqT, RespT>?, callOptions: CallOptions?, next: Channel): ClientCall<ReqT, RespT>? {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>?, headers: Metadata) {
                super.start(object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    override fun onClose(status: Status, trailers: Metadata) {
                        if (!status.isOk && trailers.containsKey(MetadataKeys.businessErrorCode)) {
                            val code = trailers.get(MetadataKeys.businessErrorCode)
                            errorConsumer(BusinessErrors.Error.valueOf(code))
                        }
                        super.onClose(status, trailers)
                    }
                }, headers)
            }
        }
    }
}