package net.crazyproger.borsch.test

import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.player.ProfileCreateServiceGrpc
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.dao.EntityID
import kotlin.sql.select
import kotlin.test.assertEquals

class ProfileCreateServiceTest {

    var app: App? = null
    var channel: ManagedChannel? = null
    var blockingStub: ProfileCreateServiceGrpc.ProfileCreateServiceBlockingStub? = null

    @Before fun init() {
        app = App().apply { start() }

        channel = ManagedChannelBuilder.forAddress("localhost", app!!.port)
                .usePlaintext(true).build()
        blockingStub = ProfileCreateServiceGrpc.newBlockingStub(channel);
    }

    @Test fun test_create_player() {
        val response = blockingStub!!.create(Empty.getDefaultInstance())
        assert(response.info.id > 0)
        assert(response.secret.isNotEmpty())

        val (insertedSecret, name) = App.database.withSession {
            val row = PlayerTable.select { PlayerTable.id eq EntityID(response.info.id, PlayerTable) }.first()
            row[PlayerTable.secret] to row[PlayerTable.name]
        }
        assertEquals(insertedSecret, response.secret)
        assertEquals(name, "Player ${response.info.id}")
    }

    @After fun tearDown() {
        App.database.withSession {
            drop(PlayerTable)
            commit()
        }
        app?.stop()
        app?.blockUntilShutdown()
    }
}