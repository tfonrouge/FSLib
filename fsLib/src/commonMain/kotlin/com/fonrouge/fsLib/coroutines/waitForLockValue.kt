package com.fonrouge.fsLib.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlin.reflect.KMutableProperty0

/**
 * Attempts to acquire a lock by setting a specified `KMutableProperty0` to `true`, retrying if necessary
 * until the lock is acquired or the number of attempts is exhausted. If the lock is acquired, the provided
 * suspend function is executed.
 *
 * @param kProperty A mutable property representing the lock state. The lock is considered acquired if
 * the property is successfully set to `true`.
 * @param attempts The number of attempts to acquire the lock before timing out. Defaults to 10.
 * @param delay The duration in milliseconds to wait between each attempt. Defaults to 100.
 * @param onLock A suspend function to execute once the lock is successfully acquired.
 * @return The result of the suspend function executed after acquiring the lock, or `null` if the lock could
 * not be acquired.
 */
@Suppress("unused")
suspend fun <R> waitForLockValue(
    kProperty: KMutableProperty0<Boolean>,
    attempts: Int = 10,
    delay: Int = 100,
    onLock: (suspend () -> R),
): R? {
    val locked: Boolean = flow<Boolean> {
        var counter = attempts
        while (kProperty.get() && counter > 0) {
            --counter
            if (attempts > 0) {
                println("waiting for lock release ... $counter attempts left")
                delay(delay.toLong())
            }
        }
        if (counter == 0) {
            emit(false)
        } else {
            kProperty.set(true)
            emit(true)
        }
    }.first()
    return if (locked) onLock().also { kProperty.set(false) } else null
}
