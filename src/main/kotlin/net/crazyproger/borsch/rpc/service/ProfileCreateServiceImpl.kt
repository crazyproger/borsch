package net.crazyproger.borsch.rpc.service

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.entity.Player
import net.crazyproger.borsch.rpc.onCompleted
import net.crazyproger.borsch.rpc.player.CreateResponseDto
import net.crazyproger.borsch.rpc.player.ProfileCreateServiceGrpc
import net.crazyproger.borsch.rpc.player.ShortInfoDto
import org.jetbrains.exposed.sql.Database
import java.util.*

class ProfileCreateServiceImpl(database: Database) : ProfileCreateServiceGrpc.ProfileCreateService, AbstractService(database) {
    companion object {
        private val DEFAULT_SHORT_INFO: ShortInfoDto = ShortInfoDto.newBuilder().setMoney(10).setName("Player").build()
    }

    override fun create(request: Empty?, responseObserver: StreamObserver<CreateResponseDto>) {
        val secretString = UUID.randomUUID().toString()
        val id = database.transaction {
            val newPlayer = Player.new {
                money = 10 // todo  should be in config
                secret = secretString
            }
            val id = newPlayer.id.value
            newPlayer.name = "Player $id"
            id
        }
        val info = ShortInfoDto.newBuilder(DEFAULT_SHORT_INFO).setId(id).build()
        val builder = CreateResponseDto.newBuilder().setSecret(secretString).setInfo(info)
        responseObserver.onCompleted(builder.build())
    }
}