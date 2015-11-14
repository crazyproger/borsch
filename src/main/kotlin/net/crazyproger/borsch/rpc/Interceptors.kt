package net.crazyproger.borsch.rpc

import io.grpc.*
import net.crazyproger.borsch.entity.PlayerTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.sql.Database
import kotlin.sql.select

open class HalfCloseListenerInterceptor(val body:
                                        (method: MethodDescriptor<*, *>, call: ServerCall<*>, () -> Unit) -> Unit
) : ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(method: MethodDescriptor<ReqT, RespT>, call: ServerCall<RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT>? {
        val originalListener = next.startCall(method, call, headers)
        return object : ForwardingServerCallListener<ReqT>() {
            override fun delegate(): ServerCall.Listener<ReqT>? = originalListener

            override fun onHalfClose() {
                body(method, call) { super.onHalfClose() }
            }
        }
    }
}

private val log: Logger = LoggerFactory.getLogger("borsch.rpc")

object LoggingInterceptor : HalfCloseListenerInterceptor(::log)

private fun log(method: MethodDescriptor<*, *>, call: ServerCall<*>, body: () -> Unit): Unit {
    log.debug("calling {}", method.fullMethodName)
    val start = System.currentTimeMillis()
    try {
        body()
    } finally {
        log.debug("finished {}, time {}ms", method.fullMethodName, System.currentTimeMillis() - start)
    }
}

object BusinessExceptionInterceptor : net.crazyproger.borsch.rpc.HalfCloseListenerInterceptor(::businessIntercept)

private fun businessIntercept(method: MethodDescriptor<*, *>, call: ServerCall<*>, body: () -> Unit): Unit {
    try {
        body()
    } catch(e: BusinessException) {
        val metadata = Metadata()
        metadata.put(MetadataKeys.businessErrorCode, e.protoError.number)
        call.close(Status.ABORTED, metadata)
        log.trace("call {} finished with business error {}", method.fullMethodName, e.protoError)
    }
}

class IdentificationInterceptor(val database: Database) : ServerInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(method: MethodDescriptor<ReqT, RespT>?, call: ServerCall<RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT>? {
        val originalListener = next.startCall(method, call, headers)
        return object : ForwardingServerCallListener<ReqT>() {
            override fun delegate(): ServerCall.Listener<ReqT>? = originalListener

            override fun onCancel() {
                PlayerIdProvider.playerIdTL.remove()
                super.onCancel()
            }

            override fun onComplete() {
                PlayerIdProvider.playerIdTL.remove()
                super.onComplete()
            }

            override fun onMessage(message: ReqT) {
                if (PlayerIdProvider.playerIdTL.get() != null) {
                    super.onMessage(message)
                    return
                }

                try {
                    val playerId = authenticate(headers)
                    PlayerIdProvider.playerIdTL.set(playerId)
                    super.onMessage(message)
                } catch(e: WrongCredentialsException) {
                    call.close(Status.UNAUTHENTICATED, Metadata());
                    throw IllegalArgumentException("Wrong credentials", e)
                }
            }
        }
    }

    private fun authenticate(metadata: Metadata): Int {
        val secretId = metadata.get(MetadataKeys.secretId) ?: throw WrongCredentialsException("no id")
        return database.withSession {
            val row = PlayerTable.select { PlayerTable.secret eq secretId }.firstOrNull() ?: throw WrongCredentialsException("bad secret")
            row[PlayerTable.id].value
        }
    }

    private class WrongCredentialsException(message: String?) : Exception(message)
}
