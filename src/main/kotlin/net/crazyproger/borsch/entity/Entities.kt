package net.crazyproger.borsch.entity

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.join

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