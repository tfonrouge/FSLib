package com.fonrouge.fsLib.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

@Suppress("unused")
suspend fun <T, R : Any> waitForLock(
    lockList: MutableList<T>,
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
