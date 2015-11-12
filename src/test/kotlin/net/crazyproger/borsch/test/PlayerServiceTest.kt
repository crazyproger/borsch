package net.crazyproger.borsch.test

import com.google.protobuf.Empty
import io.grpc.ClientInterceptors
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.entity.TABLES
import net.crazyproger.borsch.rpc.player.PlayerServiceGrpc
import net.crazyproger.borsch.rpc.player.RenameRequest
import net.crazyproger.borsch.rpc.player.RenameResponse
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.dao.EntityID
import kotlin.properties.Delegates
import kotlin.sql.insert
import kotlin.sql.select
import kotlin.test.assertEquals

class PlayerServiceTest {

    var app: App? = null
    var channel: ManagedChannel? = null
    var blockingStub: PlayerServiceGrpc.PlayerServiceBlockingStub? = null
    var secretKey = UUID.randomUUID().toString();
    var playerId by Delegates.notNull<Int>()
    val firstName = "test name"

    @Before fun init() {
        app = App().apply { start() }

        channel = ManagedChannelBuilder.forAddress("localhost", app!!.port)
                .usePlaintext(true).build()
        val intercepted = ClientInterceptors.intercept(channel, ClientIdentificationInterceptor(secretKey))
        blockingStub = PlayerServiceGrpc.newBlockingStub(intercepted);
        playerId = App.database.withSession {
            PlayerTable.insert { q ->
                q[money] = 0
                q[name] = firstName
                q[secret] = secretKey
            }.generatedKey ?: throw Exception("can't create player")
        }
    }

    @Test fun test_info() {
        val response = blockingStub!!.info(Empty.getDefaultInstance())
        assert(response.id == playerId)
        assert(response.name == firstName)
        assert(response.money == 0)

    }

    @Test fun test_rename() {
        val newName = "another name"
        val response = blockingStub!!.rename(RenameRequest.newBuilder().setName(newName).build())
        assert(response.hasInfo())
        assert(response.info.name == newName)
        assert(response.info.id == playerId)
        assertNameInDb(newName)
    }

    @Test fun test_restricted() {
        val response = blockingStub!!.rename(RenameRequest.newBuilder().setName("Player 123").build())
        assert(response.error == RenameResponse.Error.RESTRICTED)
        assertNameInDb(firstName)
    }

    @Test fun test_duplicate() {
        App.database.withSession {
            PlayerTable.insert { q ->
                q[money] = 0
                q[name] = "duplicate"
                q[secret] = "smth"
            }
        }

        val response = blockingStub!!.rename(RenameRequest.newBuilder().setName("duplicate").build())
        assert(response.error == RenameResponse.Error.DUPLICATE_NAME)
        assertNameInDb(firstName)
    }

    private fun assertNameInDb(newName: String) {
        App.database.withSession {
            val name = App.database.withSession {
                PlayerTable.select { PlayerTable.id eq EntityID(playerId, PlayerTable) }.first()[PlayerTable.name]
            }
            assertEquals(name, newName)
        }
    }

    @After fun tearDown() {
        app?.stop()
        app?.blockUntilShutdown()
        App.database.withSession {
            drop(*TABLES)
        }
    }
}