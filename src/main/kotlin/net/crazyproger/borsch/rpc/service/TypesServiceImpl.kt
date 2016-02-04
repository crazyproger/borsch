package net.crazyproger.borsch.rpc.service

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.App
import net.crazyproger.borsch.entity.ItemType
import net.crazyproger.borsch.rpc.item.ItemTypeDto
import net.crazyproger.borsch.rpc.item.TypesDto
import net.crazyproger.borsch.rpc.item.TypesServiceGrpc
import net.crazyproger.borsch.rpc.onCompleted

class TypesServiceImpl : TypesServiceGrpc.TypesService {
    override fun types(request: Empty?, responseObserver: StreamObserver<TypesDto>) {
        val all = App.database.transaction { ItemType.all().toList() }
        val dtos = all.map { ItemTypeDto.newBuilder().setId(it.id.value).setName(it.name).setPrice(it.price).build() }
        val response = TypesDto.newBuilder().addAllType(dtos).build()
        responseObserver.onCompleted(response)
    }
}