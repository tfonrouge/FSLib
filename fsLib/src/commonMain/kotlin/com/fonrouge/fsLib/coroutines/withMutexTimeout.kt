package com.fonrouge.fsLib.coroutines

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val DEFAULT_TIMEOUT_MS = 1000L
private val DEFAULT_TIMEOUT = DEFAULT_TIMEOUT_MS.milliseconds

/**
 * Executes a suspending operation with exclusive access to a shared resource protected by a mutex.
 * If the mutex cannot be acquired within the timeout period, returns null.
 *
 * @param mutex The mutex used to control access to the shared resource
 * @param timeoutDuration The maximum time to wait for acquiring the lock
 * @param operation The operation to execute with exclusive access
 * @return The result of the operation if the lock was acquired, null otherwise
 */
@Suppress("unused")
suspend fun <R> withMutexTimeout(
    mutex: Mutex,
    timeoutDuration: Duration = DEFAULT_TIMEOUT,
    operation: suspend () -> R,
): R? = try {
    withTimeout(timeoutDuration) {
        mutex.withLock {
            operation()
        }
    }
} catch (e: kotlinx.coroutines.TimeoutCancellationException) {
    null
}