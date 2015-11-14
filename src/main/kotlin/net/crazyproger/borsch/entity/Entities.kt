package net.crazyproger.borsch.entity

import kotlin.dao.Entity
import kotlin.dao.EntityClass
import kotlin.dao.EntityID
import kotlin.sql.SizedIterable

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

    companion object : EntityClass<Item>(ItemTable)
}