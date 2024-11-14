package com.fonrouge.fsLib.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

/**
 * Suspends until a specified lock is available or a number of attempts is exhausted.
 *
 * @param lockList The collection of current locks.
 * @param lockValue The specific value to lock.
 * @param attempts The number of attempts to acquire the lock before giving up. Default is 10.
 * @param delay The delay between attempts in milliseconds. Default is 100.
 * @param onCollect Function to be executed with the result of whether the lock was acquired.
 * @return The result of the onCollect function, indicating success or failure.
 */
@Suppress("unused")
suspend fun <T, R : Any> waitForLock(
    lockList: MutableCollection<T>,
    lockValue: T,
    attempts: Int = 10,
    delay: Int = 100,
    onCollect: (Boolean) -> R
): R {
    lateinit var result: R
    flow<Boolean> {
        var counter = attempts
        while (lockList.contains(lockValue) && counter > 0) {
            --counter
            if (attempts > 0) {
                println("waiting for lock release ... $counter attempts left")
                delay(delay.toLong())
            }
        }
        if (counter == 0) {
            emit(false)
        } else {
            lockList.add(lockValue)
            emit(true)
        }
    }.collect {
        result = onCollect(it)
    }
    return result
}
