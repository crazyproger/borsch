package net.crazyproger.borsch.entity

import org.jetbrains.exposed.dao.IdTable

val TABLES = arrayOf(PlayerTable, ItemTable, ItemTypeTable, ConfigTable)

object ConfigTable : IdTable() {
    //    val prefix = varchar("prefix", 128)
    val key = varchar("key", 1024).uniqueIndex()
    val stringVal = varchar("string_val", 10240).nullable()
    val intVal = integer("int_val").nullable()
}

object PlayerTable : IdTable() {
    val name = varchar("name", 255).index(true).nullable()
    val secret = varchar("secret", 1024)
    val money = integer("money")
}

object ItemTypeTable : IdTable() {
    val name = varchar("name", 255)
    val price = integer("price")
}

object ItemTable : IdTable() {
    val playerId = reference("player_id", PlayerTable)
    val typeId = reference("type_id", ItemTypeTable)
}