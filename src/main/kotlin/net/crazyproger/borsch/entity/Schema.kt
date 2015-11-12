package net.crazyproger.borsch.entity

import kotlin.dao.IdTable

val TABLES = arrayOf(PlayerTable, ItemTable, ItemType)

object PlayerTable : IdTable() {
    val name = varchar("name", 255).index(true).nullable()
    val secret = varchar("secret", 1024)
    val money = integer("money")
}

object ItemType : IdTable() {
    val name = varchar("name", 255)
    val price = integer("price")
}

object ItemTable : IdTable() {
    val playerId = reference("player_id", PlayerTable)
    val typeId = reference("type_id", ItemType)
}