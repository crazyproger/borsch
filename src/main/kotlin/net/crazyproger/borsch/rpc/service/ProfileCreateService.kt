package net.crazyproger.borsch.rpc.service

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.Player
import net.crazyproger.borsch.rpc.onCompleted
import net.crazyproger.borsch.rpc.player.CreateResponse
import net.crazyproger.borsch.rpc.player.ProfileCreateServiceGrpc
import net.crazyproger.borsch.rpc.player.ShortInfo
import java.util.*

class ProfileCreateService : ProfileCreateServiceGrpc.ProfileCreateService {
    companion object {
        private val DEFAULT_SHORT_INFO: ShortInfo = ShortInfo.newBuilder().setMoney(10).setName("Player").build()
    }

    override fun create(request: Empty?, responseObserver: StreamObserver<CreateResponse>) {
        val secretString = UUID.randomUUID().toString()
        val id = App.database.withSession {
            val newPlayer = Player.new {
                money = 10 // todo  should be in config
                secret = secretString
            }
            val id = newPlayer.id.value
            newPlayer.name = "Player $id"
            id
        }
        val info = ShortInfo.newBuilder(DEFAULT_SHORT_INFO).setId(id).build()
        val builder = CreateResponse.newBuilder().setSecret(secretString).setInfo(info)
        responseObserver.onCompleted(builder.build())
    }
}