package com.fonrouge.base.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KMutableProperty0

private val generalMutex = Mutex()

/**
 * Attempts to acquire a lock by monitoring a mutable Boolean property representing the lock state.
 * Retries are performed until the lock is available or the maximum number of attempts is reached.
 * If the lock is successfully acquired, the provided suspend function is executed, and the lock
 * is released after execution.
 *
 * @param mutex The mutex used to synchronize access to the lock state. Defaults to a general mutex.
 * @param kProperty A mutable Boolean property representing the lock state. The lock is considered acquired
 * if the property is set to `false` and then set to `true` during execution.
 * @param attempts The maximum number of attempts to acquire the lock before giving up. Defaults to 10.
 * @param delay The duration in milliseconds to wait between each attempt. Defaults to 100.
 * @param onLock A suspend function to execute once the lock is successfully acquired.
 * @return The result of the provided suspend function if the lock was successfully acquired, or null if
 * the lock could not be acquired within the maximum number of attempts.
 */
@Suppress("unused")
suspend fun <R> waitForLockValue(
    mutex: Mutex = generalMutex,
    kProperty: KMutableProperty0<Boolean>,
    attempts: Int = 10,
    delay: Int = 100,
    onLock: (suspend () -> R),
): R? {
    var counter = attempts
    return mutex.withLock {
        while (kProperty.get() && counter > 0) {
            --counter
            if (attempts > 0) {
                delay(delay.toLong())
            }
        }
        if (counter > 0) {
            kProperty.set(true)
            val r = onLock()
            kProperty.set(false)
            r
        } else {
            null
        }
    }
}
