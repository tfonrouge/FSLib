package com.fonrouge.base.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

var DEFAULT_MUTEX_TIMEOUT = 1000.milliseconds

/**
 * Executes a suspending operation with exclusive access to a resource protected by a mutex,
 * while enforcing a timeout for acquiring the lock. If the lock cannot be acquired within
 * the specified duration, the operation is aborted, and `null` is returned.
 *
 * @param mutex The mutex used to synchronize access to the resource.
 * @param timeoutDuration The maximum amount of time to wait for acquiring the mutex lock. Defaults to 1000ms.
 * @param owner An optional identifier for the owner of the lock. If `null`, no ownership is associated.
 * @param operation A suspending function that defines the operation to execute once the lock is acquired.
 * @return The result of the provided operation if the lock was successfully acquired within the timeout,
 * or `null` if the timeout occurred or an exception was thrown.
 */
@Suppress("unused")
suspend fun <R> withMutexTimeout(
    mutex: Mutex,
    timeoutDuration: Duration = DEFAULT_MUTEX_TIMEOUT,
    owner: Any? = null,
    operation: suspend () -> R,
): R? = try {
    withTimeout(timeoutDuration) {
        while (!mutex.tryLock(owner)) delay(100.milliseconds)
    }
    try {
        operation()
    } finally {
        mutex.unlock(owner)
    }
} catch (e: Exception) {
    println("Timeout error: ${e.message}")
    null
}
