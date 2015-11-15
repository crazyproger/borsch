package net.crazyproger.borsch.rpc.service

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.Item
import net.crazyproger.borsch.entity.ItemTable
import net.crazyproger.borsch.entity.PlayerTable
import net.crazyproger.borsch.rpc.PlayerIdProvider
import net.crazyproger.borsch.rpc.item.*
import net.crazyproger.borsch.rpc.onCompleted
import kotlin.dao.EntityID

class ItemsServiceImpl : ItemsServiceGrpc.ItemsService {
    private val playerId: Int by PlayerIdProvider

    override fun all(request: Empty, responseObserver: StreamObserver<ItemsDto>) {
        val items = App.database.withSession {
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

    }

    override fun sell(request: SellRequestDto, responseObserver: StreamObserver<SellResponseDto>) {
        throw UnsupportedOperationException()
    }
}