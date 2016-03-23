package net.crazyproger.borsch.rpc.service

import net.crazyproger.borsch.entity.ConfigTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import kotlin.reflect.KProperty

//private val log = LoggerFactory.getLogger("config")
// todo save default values to database if not exists
// todo cache, warm at startup(with logging)
class Config(val database: Database) {
    fun intVal(name: String? = null) = IntLoader(name, database)
    fun stringVal(name: String? = null) = StringLoader(name, database)
}

open class DefaultLoader<T : Any>(val name: String? = null, val database: Database,
                                  val column: Column<T?>, val default: (DefaultValue) -> T) {
    operator fun getValue(any: Any, property: KProperty<*>): T {
        val key = name ?: property.name.capitalize()
        val fromDB = database.transaction {
            ConfigTable.select({ ConfigTable.key eq key }).firstOrNull()?.get(column)
        }
        return fromDB ?: default(DefaultValue.valueOf(key))
    }
}

class IntLoader(name: String? = null, database: Database) :
        DefaultLoader<Int>(name, database, ConfigTable.intVal, { it.intVal!! })

class StringLoader(name: String? = null, database: Database) :
        DefaultLoader<String>(name, database, ConfigTable.stringVal, { it.stringVal!! })

enum class DefaultValue(val intVal: Int? = null, val stringVal: String? = null) {
    NamePrefix(stringVal = "Player"),
    StartMoney(intVal = 10)
}

private fun unsafeValueOf(name: String): DefaultValue? {
    try {
        return DefaultValue.valueOf(name)
    } catch(e: IllegalArgumentException) {
        return null
    }
}
