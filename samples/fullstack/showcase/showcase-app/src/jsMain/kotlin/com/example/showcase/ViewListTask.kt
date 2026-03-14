package com.example.showcase

import com.fonrouge.base.fieldName
import com.fonrouge.fullStack.config.configViewList
import com.fonrouge.fullStack.help.IHelpModule
import com.fonrouge.fullStack.tabulator.fsTabulator
import com.fonrouge.fullStack.view.ViewList
import io.kvision.core.Container
import io.kvision.tabulator.Align
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Editor
import io.kvision.tabulator.Formatter

/**
 * List view for [Task] entities.
 * Demonstrates FSLib's ViewList with Tabulator grid, column definitions,
 * toolbar, pagination, filtering, and sorting.
 */
class ViewListTask : ViewList<CommonTask, Task, String, TaskFilter, String>(
    configView = configViewList,
) {
    override val helpModule: IHelpModule = ShowcaseModule.Tasks

    companion object {
        /** View configuration wiring the list to the RPC service. */
        val configViewList = configViewList(
            viewKClass = ViewListTask::class,
            commonContainer = CommonTask,
            apiListFun = ITaskService::apiList,
        )
    }

    override fun columnDefinitionList(): List<ColumnDefinition<Task>> = listOf(
        ColumnDefinition(
            title = "ID",
            field = fieldName(Task::_id),
            headerSort = true,
            width = "80",
        ),
        ColumnDefinition(
            title = "Title",
            field = fieldName(Task::title),
            headerSort = true,
            headerFilter = Editor.INPUT,
        ),
        ColumnDefinition(
            title = "Assignee",
            field = fieldName(Task::assignee),
            headerSort = true,
            headerFilter = Editor.INPUT,
        ),
        ColumnDefinition(
            title = "Priority",
            field = fieldName(Task::priority),
            headerSort = true,
            hozAlign = Align.CENTER,
            width = "110",
            formatterFunction = { cell, _, _ ->
                val value = cell.getValue()?.toString() ?: ""
                val badge = when (value) {
                    Priority.CRITICAL -> "bg-danger"
                    Priority.HIGH -> "bg-warning text-dark"
                    Priority.MEDIUM -> "bg-info text-dark"
                    Priority.LOW -> "bg-secondary"
                    else -> "bg-light text-dark"
                }
                "<span class=\"badge $badge\">$value</span>"
            },
        ),
        ColumnDefinition(
            title = "Status",
            field = fieldName(Task::status),
            headerSort = true,
            hozAlign = Align.CENTER,
            width = "120",
            formatterFunction = { cell, _, _ ->
                val value = cell.getValue()?.toString() ?: ""
                val badge = when (value) {
                    TaskStatus.DONE -> "bg-success"
                    TaskStatus.IN_PROGRESS -> "bg-primary"
                    TaskStatus.OPEN -> "bg-info text-dark"
                    TaskStatus.CANCELLED -> "bg-dark"
                    else -> "bg-light text-dark"
                }
                "<span class=\"badge $badge\">$value</span>"
            },
        ),
        ColumnDefinition(
            title = "Est. Hours",
            field = fieldName(Task::estimatedHours),
            headerSort = true,
            hozAlign = Align.RIGHT,
            width = "110",
        ),
        columnDefinitionDeleteItem(),
    )

    override fun Container.pageListBody() {
        fsTabulator(viewList = this@ViewListTask)
    }
}
