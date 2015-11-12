package net.crazyproger.borsch.rpc

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.player.PlayerServiceGrpc
import net.crazyproger.borsch.rpc.player.RenameRequest
import net.crazyproger.borsch.rpc.player.RenameResponse
import net.crazyproger.borsch.rpc.player.ShortInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.SQLException
import kotlin.dao.EntityID
import kotlin.sql.select
import kotlin.sql.update

class PlayerService : PlayerServiceGrpc.PlayerService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(PlayerService::class.java)
    }

    // todo think: simple, but not so flexible as with provider
    private val playerId: Int by PlayerIdProvider

    override fun info(request: Empty?, responseObserver: StreamObserver<ShortInfo>) {
        log.debug("info request") // todo logging interceptor
        val info = shortInfo()
        responseObserver.onNext(info)
        responseObserver.onCompleted()
        log.debug("info request end")
    }

    private fun shortInfo(): ShortInfo {
        val builder = ShortInfo.newBuilder().setId(playerId)
        App.database.withSession {
            val row = PlayerTable.select { PlayerTable.id eq EntityID(playerId, PlayerTable) }.first()
            builder.setMoney(row[PlayerTable.money]).setName(row[PlayerTable.name])
        }
        return builder.build()
    }

    override fun rename(request: RenameRequest, responseObserver: StreamObserver<RenameResponse>) {
        log.debug("rename request")
        val name = request.name ?: throw IllegalArgumentException("bad request") //todo validation, exception to status
        if (name.startsWith("Player")) {
            responseObserver.onNext(RenameResponse.newBuilder().setError(RenameResponse.Error.RESTRICTED).build())
            responseObserver.onCompleted()
            return
        }
        try {
            App.database.withSession {
                PlayerTable.update({ PlayerTable.id eq EntityID(playerId, PlayerTable) }) {
                    it[PlayerTable.name] = request.name
                }
            }
        } catch (e: SQLException) {
            // see org.h2.api.ErrorCode.DUPLICATE_KEY_1
            if (e.errorCode == 23505) {
                responseObserver.onNext(RenameResponse.newBuilder().setError(RenameResponse.Error.DUPLICATE_NAME).build())
                responseObserver.onCompleted()
                return
            } else {
                throw throw IllegalArgumentException(e)
            }
        }
        val response = RenameResponse.newBuilder().setInfo(shortInfo()).build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
        log.debug("rename request end")
    }
}

