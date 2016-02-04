package net.crazyproger.borsch.rpc.service

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.*
import net.crazyproger.borsch.rpc.NoMoneyException
import net.crazyproger.borsch.rpc.NotFoundException
import net.crazyproger.borsch.rpc.PlayerIdProvider
import net.crazyproger.borsch.rpc.item.*
import net.crazyproger.borsch.rpc.onCompleted
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and

class ItemsServiceImpl : ItemsServiceGrpc.ItemsService {
    private val playerId: Int by PlayerIdProvider

    override fun all(request: Empty, responseObserver: StreamObserver<ItemsDto>) {
        val items = App.database.transaction {
            Item.view { ItemTable.playerId eq EntityID(playerId, PlayerTable) }.toList()
        }
        val dtos = items.map {
            ItemDto.newBuilder()
                    .setId(it.id.value)
                    .setTypeName(it.typeName)
                    .setTypeId(it.typeId.value)
                    .setSellPrice(it.typePrice / 2).build()
        }
        responseObserver.onCompleted(ItemsDto.newBuilder().addAllItem(dtos).build())
    }

    override fun buy(request: BuyRequestDto, responseObserver: StreamObserver<BuyResponseDto>) {
        val (money, type, itemId) = App.database.transaction() {
            val type = ItemType.findById(request.typeId) ?: throw NotFoundException()

            selectsForUpdate = true
            val player = Player.findById(playerId)!!
            if (player.money < type.price) throw NoMoneyException()
            player.money -= type.price
            val item = Item.new {
                this.type = type
                playerId = player.id
            }
            Triple(player.money, type, item.id.value)
        }

        val itemDto = ItemDto.newBuilder().setId(itemId).setTypeId(type.id.value).setTypeName(type.name).setSellPrice(type.price / 2).build()
        responseObserver.onCompleted(BuyResponseDto.newBuilder().setMoney(money).setItem(itemDto).build())
    }

    override fun sell(request: SellRequestDto, responseObserver: StreamObserver<SellResponseDto>) {
        val money = App.database.transaction {
            val item = Item.find(
                    { (ItemTable.id eq EntityID(request.itemId, ItemTable)) and (ItemTable.playerId eq EntityID(playerId, PlayerTable)) }
            ).firstOrNull() ?: throw NotFoundException()
            val delta = item.type.price / 2
            item.delete()
            selectsForUpdate = true
            val player = Player.findById(playerId)!!
            player.money += delta
            player.money
        }

        responseObserver.onCompleted(SellResponseDto.newBuilder().setMoney(money).build())
    }
}