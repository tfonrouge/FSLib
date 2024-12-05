package com.fonrouge.fsLib.coroutines

import kotlinx.coroutines.flow.flow
import kotlin.reflect.KMutableProperty0

@Suppress("unused")
suspend fun waitForLockValue(
    kProperty: KMutableProperty0<Boolean>,
    attempts: Int = 10,
): Boolean {
    flow<Boolean> {
        var counter = attempts
        while (kProperty.get() && counter > 0) {
            --counter
            if (attempts > 0) {
                println("waiting for lock release ... $counter attempts left")
            }
        }
        if (counter == 0) {
            emit(false)
        } else {
            emit(true)
        }
    }.collect {
        if (it) kProperty.set(true)
    }
    return kProperty.get()
}
