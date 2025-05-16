package com.fonrouge.fsLib.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val mutex = Mutex()

/**
 * Attempts to acquire a lock using a specified value in a collection, retrying if necessary
 * until the lock is acquired or the number of attempts is exhausted. If the lock is acquired,
 * the provided suspend function is executed. Optionally, the lock can be released after execution.
 *
 * @param lockList A mutable collection representing the set of active locks. The lock is considered
 * acquired if the specified value is added to the collection.
 * @param lockValue The value to be added to the collection to represent the lock.
 * @param attempts The number of attempts to acquire the lock before timing out. Defaults to 10.
 * @param delay The duration in milliseconds to wait between each attempt. Defaults to 100.
 * @param releaseLock Indicates whether to remove the lock value from the collection after execution. Defaults to true.
 * @param onLock A suspend function to execute once the lock is successfully acquired.
 * @return The result of the suspend function executed after acquiring the lock, or `null` if the lock could
 * not be acquired within the specified attempts.
 */
@Suppress("unused")
suspend fun <T, R> waitForLockList(
    lockList: MutableCollection<T>,
    lockValue: T,
    attempts: Int = 10,
    delay: Int = 100,
    releaseLock: Boolean = true,
    onLock: (suspend () -> R)
): R? {
    var counter = attempts
    return mutex.withLock {
        while (lockList.contains(lockValue) && counter > 0) {
            --counter
            if (attempts > 0) {
                println("waiting for lock release ... $counter attempts left")
                delay(delay.toLong())
            }
        }
        if (counter > 0) {
            lockList.add(lockValue)
            val r = onLock()
            lockList.remove(lockValue)
            r
        } else {
            null
        }
    }
}

