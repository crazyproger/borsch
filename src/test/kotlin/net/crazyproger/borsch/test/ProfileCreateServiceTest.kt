package net.crazyproger.borsch.test

import com.google.protobuf.Empty
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.player.ProfileCreateServiceGrpc
import org.junit.Before
import org.junit.Test
import org.jetbrains.exposed.dao.EntityID
import kotlin.properties.Delegates
import org.jetbrains.exposed.sql.select
import kotlin.test.assertEquals

class ProfileCreateServiceTest : AbstractAppTest() {

    var blockingStub by Delegates.notNull<ProfileCreateServiceGrpc.ProfileCreateServiceBlockingStub>()

    @Before override fun before() {
        super.before()
        blockingStub = ProfileCreateServiceGrpc.newBlockingStub(rawChannel);
    }

    @Test fun test_create_player() {
        val response = blockingStub.create(Empty.getDefaultInstance())
        assert(response.info.id > 0)
        assert(response.secret.isNotEmpty())

        val (insertedSecret, name) = App.database.transaction {
            val row = PlayerTable.select { PlayerTable.id eq EntityID(response.info.id, PlayerTable) }.first()
            row[PlayerTable.secret] to row[PlayerTable.name]
        }
        assertEquals(insertedSecret, response.secret)
        assertEquals(name, "Player ${response.info.id}")
    }
}