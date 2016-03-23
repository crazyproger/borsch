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

class ProfileCreateServiceImpl(database: Database, val config: Config) : ProfileCreateServiceGrpc.ProfileCreateService, AbstractService(database) {

    private val startMoney: Int by config.intVal()
    private val namePrefix: String by config.stringVal()

    override fun create(request: Empty?, responseObserver: StreamObserver<CreateResponseDto>) {
        val player = database.transaction {
            val newPlayer = Player.new {
                money = startMoney
                secret = UUID.randomUUID().toString()
                name = ""
            }
            val id = newPlayer.id.value
            newPlayer.name = "$namePrefix $id"
            newPlayer
        }
        val info = ShortInfoDto.newBuilder().setMoney(player.money).setName(player.name).setId(player.id.value).build()
        val builder = CreateResponseDto.newBuilder().setSecret(player.secret).setInfo(info)
        responseObserver.onCompleted(builder.build())
    }
}