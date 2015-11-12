package net.crazyproger.borsch.rpc

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.player.CreateResponse
import net.crazyproger.borsch.rpc.player.ProfileCreateServiceGrpc
import net.crazyproger.borsch.rpc.player.ShortInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.dao.EntityID
import kotlin.sql.insert
import kotlin.sql.update

class ProfileCreateService : ProfileCreateServiceGrpc.ProfileCreateService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(ProfileCreateService::class.java)
        private val DEFAULT_SHORT_INFO: ShortInfo = ShortInfo.newBuilder().setMoney(10).setName("Player").build()
    }

    override fun create(request: Empty?, responseObserver: StreamObserver<CreateResponse>) {
        log.debug("create player request")
        val secretString = UUID.randomUUID().toString()
        val newId: Int = App.database.withSession {
            PlayerTable.insert { q ->
                q[money] = 10 //todo  should be in config
                q[secret] = secretString
            }.generatedKey ?: throw Exception("can't create player")
        }
        App.database.withSession {
            PlayerTable.update({ PlayerTable.id eq EntityID(newId, PlayerTable) }) {
                it[PlayerTable.name] = "Player $newId"
            }
        }
        val info = ShortInfo.newBuilder(DEFAULT_SHORT_INFO).setId(newId).build()
        val builder = CreateResponse.newBuilder().setSecret(secretString).setInfo(info)
        responseObserver.onNext(builder.build())
        responseObserver.onCompleted()
        log.debug("end create player, id={}", newId)
    }
}