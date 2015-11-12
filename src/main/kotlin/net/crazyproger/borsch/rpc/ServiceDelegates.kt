package net.crazyproger.borsch.rpc

import kotlin.reflect.KProperty

internal object PlayerIdProvider {
    val playerIdTL: ThreadLocal<Int> = ThreadLocal()
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int = playerIdTL.get()
}