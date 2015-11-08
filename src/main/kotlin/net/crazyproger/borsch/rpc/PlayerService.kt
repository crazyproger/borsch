package net.crazyproger.borsch.rpc

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.player.CreateResponse
import net.crazyproger.borsch.rpc.player.PlayerGrpc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.sql.insert

class PlayerService : PlayerGrpc.Player {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(PlayerService::class.java)
    }

    override fun create(request: Empty?, responseObserver: StreamObserver<CreateResponse>) {
        log.debug("create player request")
        val secretString = UUID.randomUUID().toString()
        val newId: Int? = App.database.withSession {
            PlayerTable.insert { q ->
                q[name] = "Player"
                q[money] = 10
                q[secret] = secretString
            }.generatedKey
        }
        val builder = CreateResponse.newBuilder()
                .setId(newId ?: 0).setSecret(secretString)
        if (newId == null) builder.setError(1)
        responseObserver.onNext(builder.build())
        responseObserver.onCompleted()
        log.debug("end create player, id={}", newId)
    }
}