package com.fonrouge.fsLib.services

import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.plugin.koin
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.hasAnnotation

interface IPeriodicTaskService {
    var workingTaskMap: MutableSet<String>
}

enum class TimeUnit {
    Second,
    Minute,
    Hour,
    Day,
    Month,
    Year,
}

/**
 * Starts a periodic task runner for each function marked with @Task annotation in the [klass] parameter class
 *
 * @param klass Kotlin class which contains functions (marked with the @Task annotation) to be executed
 * @param timeUnit Time unit for periodicity [TimeUnit]
 */
@Suppress("unused")
fun Application.startPeriodicTask(
    klass: KClass<out IPeriodicTaskService>,
    timeUnit: TimeUnit = TimeUnit.Minute
) {
    koin {
        val periodicTask by inject<IPeriodicTaskService>(klass.java)
        flow {
            println("Installing ${klass.simpleName} with [$timeUnit] periodicity ...")
            while (true) {
                emit(Unit)
                delay(1000)
            }
        }
            .map { OffsetDateTime.now() }
            .distinctUntilChanged { old: OffsetDateTime, new: OffsetDateTime ->
                when (timeUnit) {
                    TimeUnit.Second -> old.second == new.second
                    TimeUnit.Minute -> old.minute == new.minute
                    TimeUnit.Hour -> old.hour == new.hour
                    TimeUnit.Day -> old.dayOfYear == new.dayOfYear
                    TimeUnit.Month -> old.month == new.month
                    TimeUnit.Year -> old.year == new.year
                }
            }.onEach {
                klass.members.forEach { kCallable ->
                    if (kCallable.hasAnnotation<Task>()) {
                        if (!periodicTask.workingTaskMap.contains(kCallable.name)) {
                            periodicTask.workingTaskMap.add(kCallable.name)
                            println("* Periodic Task Service: Starting ${klass.simpleName}::${kCallable.name}")
                            this@startPeriodicTask.launch {
                                try {
                                    kCallable.callSuspend(periodicTask)
                                } catch (e: Exception) {
                                    val msgErr = e.message ?: e.cause?.message
                                    System.err.println("* Periodic Task Service: Error ${klass.simpleName}::${kCallable.name} = $msgErr")
                                    e.printStackTrace()
                                }
                                println("* Periodic Task Service: Finalizing ${klass.simpleName}::${kCallable.name}")
                                periodicTask.workingTaskMap.remove(kCallable.name)
                            }
                        }
                    }
                }
            }.launchIn(CoroutineScope(Dispatchers.Default))
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Task
