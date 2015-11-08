package net.crazyproger.borsch.test

import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.player.PlayerGrpc
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.dao.EntityID
import kotlin.sql.select
import kotlin.test.assertEquals

class PlayerServiceTest {

    var app: App? = null
    var channel: ManagedChannel? = null
    var blockingStub: PlayerGrpc.PlayerBlockingStub? = null
    @Before fun init() {
        app = App().apply { start() }

        channel = ManagedChannelBuilder.forAddress("localhost", app!!.port)
                .usePlaintext(true).build()
        blockingStub = PlayerGrpc.newBlockingStub(channel);
    }

    @Test fun test_create_player() {
        val response = blockingStub!!.create(Empty.getDefaultInstance())
        assert(response.id > 0)
        assert(response.error == 0)
        assert(response.secret.isNotEmpty())

        val inserted = App.database.withSession {
            val select = PlayerTable.select { PlayerTable.id eq EntityID(response.id, PlayerTable) }
            select.firstOrNull()?.get(PlayerTable.secret)

        }
        assertEquals(inserted, response.secret)
    }

    @After fun tearDown() {
        app?.stop()
        app?.blockUntilShutdown()
    }
}