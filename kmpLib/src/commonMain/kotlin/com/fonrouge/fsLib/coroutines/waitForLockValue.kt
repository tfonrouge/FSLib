package com.fonrouge.fsLib.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlin.reflect.KMutableProperty0

/**
 * Suspends execution until a mutable boolean property is no longer locked or until the attempt count is exhausted.
 *
 * @param kProperty A KMutableProperty0 representing the lock status as a Boolean. It is expected to be `true` if locked and `false` otherwise.
 * @param attempts The maximum number of attempts to check the lock status before giving up. Defaults to 10.
 * @param delay The duration in milliseconds to wait between each attempt. Defaults to 100.
 *
 * @return A Boolean indicating the final lock status. Returns `true` if the lock was released, otherwise `false`.
 */
@Suppress("unused")
suspend fun waitForLockValue(
    kProperty: KMutableProperty0<Boolean>,
    attempts: Int = 10,
    delay: Int = 100,
): Boolean {
    flow<Boolean> {
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
            emit(true)
        }
    }.collect {
        if (it) kProperty.set(true)
    }
    return kProperty.get()
}
