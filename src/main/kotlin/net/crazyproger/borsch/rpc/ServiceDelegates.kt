package net.crazyproger.borsch.rpc

import io.grpc.stub.StreamObserver
import kotlin.reflect.KProperty

internal object PlayerIdProvider {
    val playerIdTL: ThreadLocal<Int> = ThreadLocal()
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int = playerIdTL.get()
}

fun <T> StreamObserver<T>.onCompleted(o: T) {
    onNext(o)
    onCompleted()
}