package net.crazyproger.borsch.test

import com.google.protobuf.Empty
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.Item
import net.crazyproger.borsch.entity.ItemType
import net.crazyproger.borsch.entity.Player
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.BusinessErrors
import net.crazyproger.borsch.rpc.item.BuyRequestDto
import net.crazyproger.borsch.rpc.item.ItemsServiceGrpc
import net.crazyproger.borsch.rpc.item.SellRequestDto
import org.junit.Before
import org.junit.Test
import org.jetbrains.exposed.dao.EntityID
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ItemsServiceTest : AbstractAppTest() {

    var service by Delegates.notNull<ItemsServiceGrpc.ItemsServiceBlockingStub>()

    var playerId by Delegates.notNull<Int>()

    var type1 by Delegates.notNull<ItemType>()
    var type2 by Delegates.notNull<ItemType>()
    var type3 by Delegates.notNull<ItemType>()

    @Before override fun before() {
        super.before()
        service = ItemsServiceGrpc.newBlockingStub(withIdentityChannel)
        playerId = App.database.transaction {
            Player.new {
                name = "Player Test"
                money = 10
                secret = secretKey
            }.id.value
        }

        App.database.transaction {
            type1 = ItemType.new { name = "type 1"; price = 1 }.apply { ItemType.reload(this) }
            type2 = ItemType.new { name = "type 2"; price = 2 }.apply { ItemType.reload(this) }
            type3 = ItemType.new { name = "type 3"; price = 12 }.apply { ItemType.reload(this) }
            Item.new { type = type1; playerId = EntityID(this@ItemsServiceTest.playerId, PlayerTable) }
            Item.new { type = type2; playerId = EntityID(this@ItemsServiceTest.playerId, PlayerTable) }
            Item.new { type = type3; playerId = EntityID(this@ItemsServiceTest.playerId, PlayerTable) }
        }
    }

    @Test fun test_get_all_types() {
        val items = service.all(Empty.getDefaultInstance())
        assertNotNull(items.itemList)
        assertEquals(3, items.itemList.size)
        assertEquals("type 1", items.getItem(0).typeName)
        assertEquals("type 2", items.getItem(1).typeName)
        assertEquals("type 3", items.getItem(2).typeName)
        assertEquals(0, items.getItem(0).sellPrice)
        assertEquals(1, items.getItem(1).sellPrice)
        assertEquals(6, items.getItem(2).sellPrice)
        assertEquals(type1.id.value, items.getItem(0).typeId)
        assertEquals(type2.id.value, items.getItem(1).typeId)
        assertEquals(type3.id.value, items.getItem(2).typeId)
    }

    @Test fun test_valid_buy() {
        val response = service.buy(BuyRequestDto.newBuilder().setTypeId(1).build())
        assertNotNull(response.item)
        assertEquals("type 1", response.item.typeName)
        assertEquals(0, response.item.sellPrice)
        App.database.transaction {
            val item = Item.findById(response.item.id)
            assertNotNull(item)
            assertEquals(playerId, item!!.playerId.value)
            val player = Player.findById(playerId)
            assertEquals(9, player!!.money)
        }
    }

    @Test fun test_invalid_buy() {
        assertBusinessError(BusinessErrors.Error.NO_MONEY) {
            withBusinessError(service) {
                buy(BuyRequestDto.newBuilder().setTypeId(3).build())
            }
        }
        App.database.transaction {
            val player = Player.findById(playerId)
            assertEquals(10, player!!.money)
            assertEquals(3, player.items.count())
        }
    }

    @Test fun test_valid_sell() {
        val response = service.sell(SellRequestDto.newBuilder().setItemId(3).build())
        assertEquals(16, response.money)
        App.database.transaction {
            assertNull(Item.findById(3))
        }
    }

    @Test fun test_restricted_sell() {
        val notMine = App.database.transaction {
            val another = Player.new { name = "another"; money = 10; secret = "sec" }
            Item.new { type = type1; playerId = another.id }.id.value
        }
        assertBusinessError(BusinessErrors.Error.NOT_FOUND) {
            withBusinessError(service) {
                sell(SellRequestDto.newBuilder().setItemId(notMine).build())
            }
        }
        App.database.transaction {
            val player = Player.findById(1)!!
            assertEquals(10, player.money)
            assertEquals(3, player.items.count())
        }
    }
}