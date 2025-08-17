package com.fonrouge.base.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val generalMutex = Mutex()

/**
 * Attempts to acquire a lock by adding a specified value to a lock list, retrying until successful or
 * until the number of attempts is exhausted. Once the lock is acquired, the provided suspend function
 * is executed. The lock is automatically released if specified.
 *
 * @param mutex The mutex used to synchronize access to the lock list. Defaults to a general mutex.
 * @param lockList A mutable collection representing the lock state. The lock is considered acquired
 * if the specified value can be added to this list.
 * @param lockValue The value used to identify the lock within the lock list.
 * @param attempts The maximum number of attempts to acquire the lock before giving up. Defaults to 10.
 * @param delay The duration in milliseconds to wait between each attempt. Defaults to 100.
 * @param releaseLock A boolean indicating whether to release the lock (remove the value from the lock list)
 * after the operation completes. Defaults to true.
 * @param onLock A suspend function to execute once the lock is successfully acquired.
 * @return The result of the provided suspend function if the lock was successfully acquired, or null if
 * the lock could not be acquired within the maximum number of attempts.
 */
@Suppress("unused")
suspend fun <T, R> waitForLockList(
    mutex: Mutex = generalMutex,
    lockList: MutableCollection<T>,
    lockValue: T,
    attempts: Int = 10,
    delay: Int = 100,
    releaseLock: Boolean = true,
    onLock: (suspend () -> R),
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

