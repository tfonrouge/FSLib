package com.example.showcase

import com.fonrouge.fullStack.config.configViewItem
import com.fonrouge.fullStack.help.IHelpModule
import com.fonrouge.fullStack.layout.formColumn
import com.fonrouge.fullStack.layout.formRow
import com.fonrouge.fullStack.view.ViewFormPanel
import com.fonrouge.fullStack.view.ViewItem
import com.fonrouge.fullStack.view.viewFormPanel
import io.kvision.core.Container
import io.kvision.form.select.TomSelect
import io.kvision.form.text.Text
import io.kvision.form.text.TextArea
import io.kvision.form.number.Spinner

/**
 * Item view for [Task] entities.
 * Demonstrates FSLib's ViewItem with form panel, field binding,
 * layout helpers (formRow/formColumn), and CRUD lifecycle.
 */
class ViewItemTask : ViewItem<CommonTask, Task, String, TaskFilter>(
    configView = configViewItem,
) {
    override val helpModule: IHelpModule = ShowcaseModule.Tasks

    companion object {
        /** View configuration wiring the item form to the RPC service. */
        val configViewItem = configViewItem(
            viewKClass = ViewItemTask::class,
            commonContainer = CommonTask,
            apiItemFun = ITaskService::apiItem,
        )
    }

    override fun Container.pageItemBody(): ViewFormPanel<Task> {
        return viewFormPanel(viewItem = this@ViewItemTask) {
            formRow {
                formColumn(6) {
                    add(
                        Task::title,
                        Text(label = "Title"),
                        required = true,
                        requiredMessage = "Title is required",
                    )
                }
                formColumn(6) {
                    add(
                        Task::assignee,
                        Text(label = "Assignee"),
                    )
                }
            }
            formRow {
                formColumn(4) {
                    add(
                        Task::priority,
                        TomSelect(
                            options = Priority.entries.map { it to it },
                            label = "Priority",
                        ),
                        required = true,
                    )
                }
                formColumn(4) {
                    add(
                        Task::status,
                        TomSelect(
                            options = TaskStatus.entries.map { it to it },
                            label = "Status",
                        ),
                        required = true,
                    )
                }
                formColumn(4) {
                    add(
                        Task::estimatedHours,
                        Spinner(label = "Estimated Hours", min = 0, max = 1000, step = 1),
                    )
                }
            }
            formRow {
                formColumn(12) {
                    add(
                        Task::description,
                        TextArea(label = "Description", rows = 3),
                    )
                }
            }
        }
    }
}
