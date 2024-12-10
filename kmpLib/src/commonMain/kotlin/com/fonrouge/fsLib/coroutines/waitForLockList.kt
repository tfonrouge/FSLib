package com.fonrouge.fsLib.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

/**
 * Attempts to acquire a lock by adding a specified value to a collection, retrying if necessary until the lock is acquired or the attempt count is exhausted.
 *
 * @param lockList The mutable collection representing the current locks.
 * @param lockValue The value to be added to the lockList to acquire the lock.
 * @param attempts The number of attempts to acquire the lock before giving up. Defaults to 10.
 * @param delay The delay in milliseconds between attempts to acquire the lock. Defaults to 100.
 * @param onLock An optional suspend function to execute once the lock is acquired.
 * @return A Boolean indicating whether the lock was successfully acquired. Returns `true` if the lock was acquired, otherwise `false`.
 */
@Suppress("unused")
suspend fun <T> waitForLockList(
    lockList: MutableCollection<T>,
    lockValue: T,
    attempts: Int = 10,
    delay: Int = 100,
    onLock: (suspend () -> Unit)? = null,
): Boolean {
    var result = false
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
        result = it
        onLock?.let {
            it()
            lockList.remove(lockValue)
        }
    }
    return result
}
