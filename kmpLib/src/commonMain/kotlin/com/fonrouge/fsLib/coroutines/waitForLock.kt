package com.fonrouge.fsLib.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

/**
 * Suspends execution while waiting for a lock to be released from a specified lock list, and then performs an action.
 *
 * @param lockList The collection of locks to check against.
 * @param lockValue The specific lock value to wait for.
 * @param attempts The number of attempts to check for the lock release before giving up. Default is 10.
 * @param delay The time to wait between attempts in milliseconds. Default is 100.
 * @param onCollect The action to perform once the lock is either acquired or the attempts are exhausted.
 *
 * @return The result of the action performed by the `onCollect` function.
 */
@Suppress("unused")
suspend fun <T, R : Any> waitForLock(
    lockList: MutableCollection<T>,
    lockValue: T,
    attempts: Int = 10,
    delay: Int = 100,
    onCollect: suspend (Boolean) -> R
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
