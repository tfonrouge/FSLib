package com.example.showcase

import com.fonrouge.base.api.ApiFilter
import com.fonrouge.base.common.simpleCommon
import com.fonrouge.fullStack.config.configView
import com.fonrouge.fullStack.view.View
import io.kvision.core.Container
import io.kvision.html.*

/**
 * A non-data view demonstrating the use of [View] with [com.fonrouge.base.common.ICommon]
 * (not [com.fonrouge.base.common.ICommonContainer]).
 *
 * This pattern is useful for landing pages, dashboards, settings screens, or any view
 * that does not manage a data model. The [ApiFilter] here acts as lightweight view state
 * rather than a data query filter.
 */
class ViewHome : View<ApiFilter>(
    configView = configViewHome,
) {
    companion object {
        /** Common metadata for the home view — label and filter only, no data model. */
        val commonHome = simpleCommon(label = "Home")

        /** View configuration for the home page. */
        val configViewHome = configView(
            viewKClass = ViewHome::class,
            commonContainer = commonHome,
            baseUrl = "Home",
        )
    }

    override fun Container.displayPage() {
        div(className = "container mt-4") {
            div(className = "text-center mb-4") {
                h1(content = "FSLib Showcase")
                p(content = "A demonstration of FSLib's full-stack CRUD framework.", className = "lead")
            }
            div(className = "row") {
                div(className = "col-md-6") {
                    div(className = "card mb-3") {
                        div(className = "card-body") {
                            h5(content = "Task List", className = "card-title")
                            p(
                                content = "Browse, filter, and sort tasks using a Tabulator-powered data grid.",
                                className = "card-text",
                            )
                            link(
                                label = "Open Task List",
                                url = "#/ViewListTask",
                                className = "btn btn-primary",
                            )
                        }
                    }
                }
                div(className = "col-md-6") {
                    div(className = "card mb-3") {
                        div(className = "card-body") {
                            h5(content = "Create Task", className = "card-title")
                            p(
                                content = "Create a new task using the form panel with validation and layout helpers.",
                                className = "card-text",
                            )
                            link(
                                label = "New Task",
                                url = "#/ViewItemTask?action=Create",
                                className = "btn btn-success",
                            )
                        }
                    }
                }
            }
        }
    }
}
