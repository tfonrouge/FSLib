package com.example.ssrsample.advanced

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import kotlinx.serialization.Serializable

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

// ── Common Containers ───────────────────────────────────────

/** Metadata container for [Project]. */
object CommonProject : ICommonContainer<Project, String, ApiFilter>(
    itemKClass = Project::class,
    filterKClass = ApiFilter::class,
    labelItem = "Project",
    labelList = "Projects",
)

/** Metadata container for [Task]. */
object CommonTask : ICommonContainer<Task, String, ApiFilter>(
    itemKClass = Task::class,
    filterKClass = ApiFilter::class,
    labelItem = "Task",
    labelList = "Tasks",
)
