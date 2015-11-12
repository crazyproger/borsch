package net.crazyproger.borsch.rpc

import io.grpc.*
import net.crazyproger.borsch.entity.PlayerTable
import kotlin.sql.Database
import kotlin.sql.select

class IdentificationInterceptor(val database: Database) : ServerInterceptor {

    companion object {
        val SECRET_ID_KEY: Metadata.Key<String> = Metadata.Key.of("secret_id", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun <ReqT : Any?, RespT : Any?> interceptCall(method: MethodDescriptor<ReqT, RespT>?, call: ServerCall<RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT>? {

        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(method, call, headers)) {
            override fun onCancel() {
                PlayerIdProvider.playerIdTL.remove()
                super.onCancel()
            }

            override fun onComplete() {
                PlayerIdProvider.playerIdTL.remove()
                super.onComplete()
            }

            override fun onMessage(message: ReqT) {
                try {
                    val playerId = authenticate(headers)
                    PlayerIdProvider.playerIdTL.set(playerId)
                    return super.onMessage(message)
                } catch(e: WrongCredentialsException) {
                    call.close(Status.UNAUTHENTICATED, null);
                    throw IllegalArgumentException("Wrong credentials", e)
                }
            }
        }
    }

    private fun authenticate(metadata: Metadata): Int {
        val secretId = metadata.get(SECRET_ID_KEY) ?: throw WrongCredentialsException("no id")
        return database.withSession {
            val row = PlayerTable.select { PlayerTable.secret eq secretId }.firstOrNull() ?: throw WrongCredentialsException("bad secret")
            row[PlayerTable.id].value
        }
    }
}

class WrongCredentialsException(message: String?) : Exception(message)