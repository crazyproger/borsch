package net.crazyproger.borsch.test

import com.google.protobuf.Empty
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.BusinessErrors
import net.crazyproger.borsch.rpc.player.PlayerServiceGrpc
import net.crazyproger.borsch.rpc.player.RenameRequestDto
import org.junit.Before
import org.junit.Test
import org.jetbrains.exposed.dao.EntityID
import kotlin.properties.Delegates
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import kotlin.test.assertEquals

class PlayerServiceTest : AbstractAppTest() {

    var blockingStub: PlayerServiceGrpc.PlayerServiceBlockingStub? = null
    var playerId by Delegates.notNull<Int>()
    val firstName = "test name"

    @Before override fun before() {
        super.before()
        blockingStub = PlayerServiceGrpc.newBlockingStub(withIdentityChannel);
        playerId = App.database.transaction {
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
        val response = blockingStub!!.rename(RenameRequestDto.newBuilder().setName(newName).build())
        assert(response.name == newName)
        assert(response.id == playerId)
        assertNameInDb(newName)
    }

    @Test fun test_restricted() {
        assertBusinessError(BusinessErrors.Error.RESTRICTED) {
            withBusinessError(blockingStub!!) {
                rename(RenameRequestDto.newBuilder().setName("Player 123").build())
            }
        }
        assertNameInDb(firstName)
    }

    @Test fun test_duplicate() {
        App.database.transaction {
            PlayerTable.insert { q ->
                q[money] = 0
                q[name] = "duplicate"
                q[secret] = "smth"
            }
        }

        assertBusinessError(BusinessErrors.Error.DUPLICATE_NAME) {
            withBusinessError(blockingStub!!) {
                rename(RenameRequestDto.newBuilder().setName("duplicate").build())
            }
        }
        assertNameInDb(firstName)
    }
    private fun assertNameInDb(newName: String) {
        App.database.transaction {
            val name = App.database.transaction {
                PlayerTable.select { PlayerTable.id eq EntityID(playerId, PlayerTable) }.first()[PlayerTable.name]
            }
            assertEquals(name, newName)
        }
    }
}