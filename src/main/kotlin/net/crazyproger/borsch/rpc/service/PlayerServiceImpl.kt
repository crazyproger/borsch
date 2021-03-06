package net.crazyproger.borsch.rpc.service

import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.entity.Player
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.*
import net.crazyproger.borsch.rpc.player.PlayerServiceGrpc
import net.crazyproger.borsch.rpc.player.RenameRequestDto
import net.crazyproger.borsch.rpc.player.ShortInfoDto
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.update
import java.sql.SQLException

class PlayerServiceImpl(database: Database) : PlayerServiceGrpc.PlayerService, AbstractService(database) {

    // todo think: simple, but not so flexible as with provider
    private val playerId: Int by PlayerIdProvider

    override fun info(request: Empty?, responseObserver: StreamObserver<ShortInfoDto>) =
            responseObserver.onCompleted(ShortInfoDto())

    private fun ShortInfoDto(): ShortInfoDto {
        val player = database.transaction { Player.findById(playerId) } ?: throw NotFoundException()
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
            database.transaction {
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

