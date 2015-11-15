package net.crazyproger.borsch.entity

import kotlin.dao.Entity
import kotlin.dao.EntityClass
import kotlin.dao.EntityID
import kotlin.sql.ColumnSet
import kotlin.sql.JoinType
import kotlin.sql.SizedIterable
import kotlin.sql.join

class Player(id: EntityID) : Entity(id) {
    var name by PlayerTable.name
    var secret by PlayerTable.secret
    var money by PlayerTable.money

    val items: SizedIterable<Item> by Item.referrersOn(ItemTable.playerId)

    companion object : EntityClass<Player>(PlayerTable)
}

class ItemType(id: EntityID) : Entity(id) {
    var name by ItemTypeTable.name
    var price by ItemTypeTable.price

    companion object : EntityClass<ItemType>(ItemTypeTable)
}

class Item(id: EntityID) : Entity(id) {
    var type by ItemType referencedOn ItemTable.typeId
    var typeName by ItemTypeTable.name
    var typeId by ItemTypeTable.id
    var typePrice by ItemTypeTable.price
    var playerId by ItemTable.playerId

    companion object : EntityClass<Item>(ItemTable) {
        override val dependsOnTables: ColumnSet
            get() = table.join(ItemTypeTable, JoinType.INNER, ItemTable.typeId, ItemTypeTable.id)
    }
}