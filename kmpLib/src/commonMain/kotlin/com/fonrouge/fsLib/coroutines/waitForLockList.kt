package com.fonrouge.fsLib.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

/**
 * Attempts to acquire a lock by adding a specified value to a collection, retrying if necessary until the lock
 * is acquired or the number of attempts is exhausted. If the lock is acquired, the provided suspend function
 * is executed.
 *
 * @param lockList The mutable collection of locks representing current locked items.
 * @param lockValue The value to be added to the lockList to attempt acquiring the lock.
 * @param attempts The number of attempts to acquire the lock before timing out. Defaults to 10.
 * @param delay The duration in milliseconds to wait between each attempt. Defaults to 100.
 * @param onLock A suspend function to execute once the lock is successfully acquired.
 * @return The result of the suspend function executed after acquiring the lock, or `null` if the lock could not be acquired.
 */
@Suppress("unused")
suspend fun <T, R> waitForLockList(
    lockList: MutableCollection<T>,
    lockValue: T,
    attempts: Int = 10,
    delay: Int = 100,
    onLock: (suspend () -> R),
): R? {
    val lock = flow<Boolean> {
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
    }.firstOrNull()
    val result = if (lock == true) {
        onLock().also {
            lockList.remove(lockValue)
        }
    } else null
    return result
}
