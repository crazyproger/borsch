package net.crazyproger.borsch.rpc

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.player.PlayerServiceGrpc
import net.crazyproger.borsch.rpc.player.RenameRequest
import net.crazyproger.borsch.rpc.player.RenameResponse
import net.crazyproger.borsch.rpc.player.ShortInfo
import java.sql.SQLException
import kotlin.dao.EntityID
import kotlin.sql.select
import kotlin.sql.update

class PlayerService : PlayerServiceGrpc.PlayerService {

    // todo think: simple, but not so flexible as with provider
    private val playerId: Int by PlayerIdProvider

    override fun info(request: Empty?, responseObserver: StreamObserver<ShortInfo>) {
        val info = shortInfo()
        responseObserver.onNext(info)
        responseObserver.onCompleted()
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
    }
}

