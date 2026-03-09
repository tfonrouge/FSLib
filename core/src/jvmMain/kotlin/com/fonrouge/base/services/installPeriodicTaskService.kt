package com.fonrouge.base.services

import com.fonrouge.base.services.Task.TimeUnit
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.KCallable
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation

interface IPeriodicTaskService {
    var workingTaskMap: MutableSet<String>
}

/**
 * Installs a periodic task service of type [T] within the application.
 * The service will periodically execute tasks annotated with [Task] based on the specified time unit.
 *
 * @param T The type of the periodic task service that implements [IPeriodicTaskService].
 * @param debug If set to true, debug information will be printed to the console.
 */
@Suppress("unused")
fun <T : IPeriodicTaskService> Application.installPeriodicTaskService(
    periodicTask: T,
    debug: Boolean = false,
) {
    val klass = periodicTask::class
    val pairPeriodFunc: List<Pair<TimeUnit, KCallable<*>>> = klass.members.mapNotNull { kFunction ->
        kFunction.findAnnotation<Task>()?.let { task: Task ->
            task.timeUnit to kFunction
        }
    }
    var previousTime = OffsetDateTime.MIN
    flow {
        if (debug) {
            println("Installing Periodic Task Service ${klass.simpleName} ...")
        }
        while (true) {
            this.emit(OffsetDateTime.now())
            delay(200)
        }
    }.onEach { now: OffsetDateTime ->
        val funcsToCall = mutableListOf<KCallable<*>>()
        if (previousTime.second != now.second) {
            TimeUnit.entries.forEach { timeUnit ->
                val funcsForTimeUnit: List<KCallable<*>> =
                    pairPeriodFunc.filter { it.first == timeUnit }.map { it.second }
                when (timeUnit) {
                    TimeUnit.Second -> funcsToCall.addAll(funcsForTimeUnit)
                    TimeUnit.Minute -> if (previousTime.minute != now.minute) funcsToCall.addAll(
                        funcsForTimeUnit
                    )

                    TimeUnit.Hour -> if (previousTime.hour != now.hour) funcsToCall.addAll(
                        funcsForTimeUnit
                    )

                    TimeUnit.Day -> if (previousTime.dayOfYear != now.dayOfYear) funcsToCall.addAll(
                        funcsForTimeUnit
                    )

                    TimeUnit.Month -> if (previousTime.monthValue != now.monthValue) funcsToCall.addAll(
                        funcsForTimeUnit
                    )

                    TimeUnit.Year -> if (previousTime.year != now.year) funcsToCall.addAll(
                        funcsForTimeUnit
                    )
                }
            }
            previousTime = now
        }
        funcsToCall.forEach { kCallable ->
            if (!periodicTask.workingTaskMap.contains(kCallable.name)) {
                periodicTask.workingTaskMap.add(kCallable.name)
                if (debug) {
                    println("[${LocalDateTime.now()}] Periodic Task Service: Starting ${klass.simpleName}::${kCallable.name}")
                }
                launch {
                    try {
                        kCallable.callSuspend(periodicTask)
                    } catch (e: Exception) {
                        val msgErr = e.message ?: e.cause?.message
                        System.err.println("[${LocalDateTime.now()}] Periodic Task Service: Error ${klass.simpleName}::${kCallable.name} = $msgErr")
                        e.printStackTrace()
                    }
                    if (debug) {
                        println("[${LocalDateTime.now()}] Periodic Task Service: Finalizing ${klass.simpleName}::${kCallable.name}")
                    }
                    periodicTask.workingTaskMap.remove(kCallable.name)
                }
            }
        }
    }.launchIn(CoroutineScope(Dispatchers.Default))
}

/**
 * @param timeUnit Time unit for periodicity [TimeUnit]
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Task(
    val timeUnit: TimeUnit,
) {
    enum class TimeUnit {
        Second,
        Minute,
        Hour,
        Day,
        Month,
        Year,
    }
}
