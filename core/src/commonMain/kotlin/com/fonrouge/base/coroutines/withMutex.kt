package com.fonrouge.base.coroutines

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Executes a suspending operation with exclusive access to a shared resource protected by a mutex.
 * If the mutex is already locked, the specified `ifLocked` suspend function is executed instead.
 *
 * @param ifLocked A suspend function to execute if the mutex is already locked. Defaults to throwing
 * an IllegalStateException.
 * @param block The main suspending operation to be executed with the mutex lock.
 * @return The result of the `block` or `ifLocked` suspend function, depending on whether the mutex
 * was already locked.
 */
@Suppress("unused")
suspend fun <R> Mutex.withMutex(
    ifLocked: () -> R = { throw IllegalStateException("Mutex is already locked") },
    block: suspend () -> R
): R = if (isLocked) ifLocked() else withLock { block() }
