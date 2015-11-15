package net.crazyproger.borsch.test

import com.google.protobuf.Empty
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.Item
import net.crazyproger.borsch.entity.ItemType
import net.crazyproger.borsch.entity.Player
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.item.ItemsServiceGrpc
import org.junit.Before
import org.junit.Test
import kotlin.dao.EntityID
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ItemsServiceTest : AbstractAppTest() {

    var serviceBlockingStub by Delegates.notNull<ItemsServiceGrpc.ItemsServiceBlockingStub>()

    var playerId by Delegates.notNull<Int>()

    var type1 by Delegates.notNull<ItemType>()
    var type2 by Delegates.notNull<ItemType>()
    var type3 by Delegates.notNull<ItemType>()

    @Before override fun before() {
        super.before()
        serviceBlockingStub = ItemsServiceGrpc.newBlockingStub(withIdentityChannel)
        playerId = App.database.withSession {
            Player.new {
                name = "Player Test"
                money = 10
                secret = secretKey
            }.id.value
        }

        App.database.withSession {
            type1 = ItemType.new { name = "type 1"; price = 1 }.apply { ItemType.reload(this) }
            type2 = ItemType.new { name = "type 2"; price = 2 }.apply { ItemType.reload(this) }
            type3 = ItemType.new { name = "type 3"; price = 4 }.apply { ItemType.reload(this) }
            Item.new { type = type1; playerId = EntityID(this@ItemsServiceTest.playerId, PlayerTable) }
            Item.new { type = type2; playerId = EntityID(this@ItemsServiceTest.playerId, PlayerTable) }
            Item.new { type = type3; playerId = EntityID(this@ItemsServiceTest.playerId, PlayerTable) }
        }
    }

    @Test fun test_get_all_types() {
        val items = serviceBlockingStub.all(Empty.getDefaultInstance())
        assertNotNull(items.itemList)
        assertEquals(3, items.itemList.size)
        assertEquals(items.getItem(0).typeName, "type 1")
        assertEquals(items.getItem(1).typeName, "type 2")
        assertEquals(items.getItem(2).typeName, "type 3")
        assertEquals(items.getItem(0).sellPrice, 0)
        assertEquals(items.getItem(1).sellPrice, 1)
        assertEquals(items.getItem(2).sellPrice, 2)
        assertEquals(items.getItem(0).typeId, type1.id.value)
        assertEquals(items.getItem(1).typeId, type2.id.value)
        assertEquals(items.getItem(2).typeId, type3.id.value)
    }
}