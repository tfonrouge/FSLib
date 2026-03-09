package com.example.ssrsample.advanced

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

// ── Models ──────────────────────────────────────────────────

/**
 * A project that groups tasks.
 */
@Serializable
data class Project(
    override val _id: String = "",
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val status: String = "active",
    val owner: String = "",
) : BaseDoc<String>

/**
 * A task belonging to a project.
 */
@Serializable
data class Task(
    override val _id: String = "",
    val title: String = "",
    val projectCode: String = "",
    val priority: String = "medium",
    val status: String = "open",
    val assignee: String = "",
    val estimate: Double = 0.0,
    val description: String = "",
) : BaseDoc<String>

// ── Filters ─────────────────────────────────────────────────

/** API filter for Project queries. */
@Serializable
class ProjectFilter : IApiFilter<String>()

/** API filter for Task queries. */
@Serializable
class TaskFilter : IApiFilter<String>()

// ── Common Containers ───────────────────────────────────────

/** Metadata container for [Project]. */
object CommonProject : ICommonContainer<Project, String, ProjectFilter>(
    itemKClass = Project::class,
    idSerializer = String.serializer(),
    apiFilterSerializer = ProjectFilter.serializer(),
    labelItem = "Project",
    labelList = "Projects",
)

/** Metadata container for [Task]. */
object CommonTask : ICommonContainer<Task, String, TaskFilter>(
    itemKClass = Task::class,
    idSerializer = String.serializer(),
    apiFilterSerializer = TaskFilter.serializer(),
    labelItem = "Task",
    labelList = "Tasks",
)
