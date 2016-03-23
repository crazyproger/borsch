package net.crazyproger.borsch.rpc.service

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.crazyproger.borsch.entity.ItemType
import net.crazyproger.borsch.rpc.item.ItemTypeDto
import net.crazyproger.borsch.rpc.item.TypesDto
import net.crazyproger.borsch.rpc.item.TypesServiceGrpc
import net.crazyproger.borsch.rpc.onCompleted
import org.jetbrains.exposed.sql.Database

class TypesServiceImpl(database: Database) : TypesServiceGrpc.TypesService, AbstractService(database) {
    override fun types(request: Empty?, responseObserver: StreamObserver<TypesDto>) {
        val all = database.transaction { ItemType.all().toList() }
        val dtos = all.map { ItemTypeDto.newBuilder().setId(it.id.value).setName(it.name).setPrice(it.price).build() }
        val response = TypesDto.newBuilder().addAllType(dtos).build()
        responseObserver.onCompleted(response)
    }
}