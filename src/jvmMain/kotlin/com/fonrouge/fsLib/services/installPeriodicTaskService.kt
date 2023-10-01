package com.fonrouge.fsLib.services

import com.fonrouge.fsLib.services.Task.TimeUnit
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import java.time.OffsetDateTime
import kotlin.reflect.KCallable
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation

interface IPeriodicTaskService {
    var workingTaskMap: MutableSet<String>
}

/**
 * Starts a periodic task runner for each function marked with @Task annotation in the [T] parameter class
 *
 * @param debug Shows some info when running tasks
 */
@Suppress("unused")
inline fun <reified T : IPeriodicTaskService> Application.installPeriodicTaskService(
    debug: Boolean = false,
) {
    val periodicTask: T by inject<T>()
    val klass = periodicTask::class
    println("periodicTask working task map = ${periodicTask.workingTaskMap}")
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

                    TimeUnit.Hour -> if (previousTime.hour != now.hour) funcsToCall.addAll(funcsForTimeUnit)
                    TimeUnit.Day -> if (previousTime.dayOfYear != now.dayOfYear) funcsToCall.addAll(
                        funcsForTimeUnit
                    )

                    TimeUnit.Month -> if (previousTime.monthValue != now.monthValue) funcsToCall.addAll(
                        funcsForTimeUnit
                    )

                    TimeUnit.Year -> if (previousTime.year != now.year) funcsToCall.addAll(funcsForTimeUnit)
                }
            }
            previousTime = now
        }
        funcsToCall.forEach { kCallable ->
            if (!periodicTask.workingTaskMap.contains(kCallable.name)) {
                periodicTask.workingTaskMap.add(kCallable.name)
                if (debug) {
                    println("* Periodic Task Service: Starting ${klass.simpleName}::${kCallable.name}")
                }
                launch {
                    try {
                        kCallable.callSuspend(periodicTask)
                    } catch (e: Exception) {
                        val msgErr = e.message ?: e.cause?.message
                        System.err.println("* Periodic Task Service: Error ${klass.simpleName}::${kCallable.name} = $msgErr")
                        e.printStackTrace()
                    }
                    if (debug) {
                        println("* Periodic Task Service: Finalizing ${klass.simpleName}::${kCallable.name}")
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
