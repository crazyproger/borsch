package net.crazyproger.borsch.rpc.service

import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.Player
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.*
import net.crazyproger.borsch.rpc.player.PlayerServiceGrpc
import net.crazyproger.borsch.rpc.player.RenameRequestDto
import net.crazyproger.borsch.rpc.player.ShortInfoDto
import java.sql.SQLException
import kotlin.dao.EntityID
import kotlin.sql.update

class PlayerServiceImpl : PlayerServiceGrpc.PlayerService {

    // todo think: simple, but not so flexible as with provider
    private val playerId: Int by PlayerIdProvider

    override fun info(request: Empty?, responseObserver: StreamObserver<ShortInfoDto>) =
            responseObserver.onCompleted(ShortInfoDto())

    private fun ShortInfoDto(): ShortInfoDto {
        val player = App.database.withSession { Player.findById(playerId) } ?: throw NotFoundException()
        return ShortInfoDto.newBuilder().setId(playerId).setName(player.name).setMoney(player.money).build()
    }

    override fun rename(request: RenameRequestDto, responseObserver: StreamObserver<ShortInfoDto>) {
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
        responseObserver.onCompleted(ShortInfoDto())
    }
}

