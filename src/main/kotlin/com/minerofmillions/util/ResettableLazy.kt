package com.minerofmillions.util

import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

fun <T> resettableLazyVal(initializer: () -> T) = ResettableDelegate(initializer)
fun <T> resettableLazyVar(initializer: () -> T) = MutableResettableDelegate(initializer)

class ResettableDelegate<T>(private val initializer: () -> T) {
    private val lazyRef: AtomicReference<Lazy<T>> = AtomicReference(
        lazy(
            initializer
        )
    )

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return lazyRef.get().getValue(thisRef, property)
    }

    fun reset() {
        lazyRef.set(lazy(initializer))
    }
}

class MutableResettableDelegate<T>(private val initializer: () -> T) {
    private val lazyRef: AtomicReference<Lazy<T>> = AtomicReference(
        lazy(
            initializer
        )
    )

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return lazyRef.get().getValue(thisRef, property)
    }

    fun reset() {
        lazyRef.set(lazy(initializer))
    }
}