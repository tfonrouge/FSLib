package com.example.showcase

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.Serializable

/**
 * Priority levels for a task.
 */
object Priority {
    const val LOW = "Low"
    const val MEDIUM = "Medium"
    const val HIGH = "High"
    const val CRITICAL = "Critical"

    /** All available priority values. */
    val entries = listOf(LOW, MEDIUM, HIGH, CRITICAL)
}

/**
 * Status values for a task.
 */
object TaskStatus {
    const val OPEN = "Open"
    const val IN_PROGRESS = "InProgress"
    const val DONE = "Done"
    const val CANCELLED = "Cancelled"

    /** All available status values. */
    val entries = listOf(OPEN, IN_PROGRESS, DONE, CANCELLED)
}

/**
 * A task entry used to demonstrate FSLib's CRUD features.
 */
@Serializable
data class Task(
    override val _id: String = "",
    val title: String = "",
    val description: String = "",
    val assignee: String = "",
    val priority: String = Priority.MEDIUM,
    val status: String = TaskStatus.OPEN,
    val estimatedHours: Int = 0,
) : BaseDoc<String>

/**
 * Metadata container for [Task].
 */
object CommonTask : ICommonContainer<Task, String, ApiFilter>(
    itemKClass = Task::class,
    filterKClass = ApiFilter::class,
    labelItem = "Task",
    labelList = "Tasks",
    labelId = { it?.let { "${it.title} (${it._id})" } ?: "<no-task>" },
)
