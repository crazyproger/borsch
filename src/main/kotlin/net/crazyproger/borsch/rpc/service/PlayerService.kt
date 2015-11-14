package net.crazyproger.borsch.rpc.service

import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.DuplicateNameException
import net.crazyproger.borsch.rpc.PlayerIdProvider
import net.crazyproger.borsch.rpc.RestrictedException
import net.crazyproger.borsch.rpc.onCompleted
import net.crazyproger.borsch.rpc.player.PlayerServiceGrpc
import net.crazyproger.borsch.rpc.player.RenameRequest
import net.crazyproger.borsch.rpc.player.ShortInfo
import java.sql.SQLException
import kotlin.dao.EntityID
import kotlin.sql.select
import kotlin.sql.update

class PlayerService : PlayerServiceGrpc.PlayerService {

    // todo think: simple, but not so flexible as with provider
    private val playerId: Int by PlayerIdProvider

    override fun info(request: Empty?, responseObserver: StreamObserver<ShortInfo>) =
            responseObserver.onCompleted(shortInfo())

    private fun shortInfo(): ShortInfo {
        val builder = ShortInfo.newBuilder().setId(playerId)
        App.database.withSession {
            val row = PlayerTable.select { PlayerTable.id eq EntityID(playerId, PlayerTable) }.first()
            builder.setMoney(row[PlayerTable.money]).setName(row[PlayerTable.name])
        }
        return builder.build()
    }

    override fun rename(request: RenameRequest, responseObserver: StreamObserver<ShortInfo>) {
        // todo validation?
        if (request.name.isNullOrBlank()) {
            throw Status.INVALID_ARGUMENT.asException()
        }
        if (request.name.startsWith("Player")) {
            throw RestrictedException()
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
                throw DuplicateNameException()
            } else {
                throw IllegalArgumentException(e)
            }
        }
        responseObserver.onCompleted(shortInfo())
    }
}

