package com.fonrouge.fsUtils.view.changeLog

import com.fonrouge.base.common.ICommonChangeLog
import com.fonrouge.base.enums.find
import com.fonrouge.base.fieldName
import com.fonrouge.base.lib.toDateTimeString
import com.fonrouge.base.model.ChangeLogFilter
import com.fonrouge.base.model.IChangeLog
import com.fonrouge.base.model.IUser
import com.fonrouge.base.types.OId
import com.fonrouge.fullStack.config.ConfigViewItem
import com.fonrouge.fullStack.config.ConfigViewList
import com.fonrouge.fullStack.layout.addPageListBody
import com.fonrouge.fullStack.tabulator.fsTabulator
import com.fonrouge.fullStack.tabulator.getDataDate
import com.fonrouge.fullStack.tabulator.getDataValue
import com.fonrouge.fullStack.tabulator.menuItem
import com.fonrouge.fullStack.view.ViewList
import io.kvision.core.Container
import io.kvision.form.text.textAreaInput
import io.kvision.html.span
import io.kvision.modal.Modal
import io.kvision.modal.ModalSize
import io.kvision.tabulator.Align
import io.kvision.tabulator.ColumnDefinition
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlin.js.json

/**
 * Abstract class representing a view for the list of change logs. It provides functionality for
 * displaying, configuring, and managing change log data within the context of a specific implementation.
 *
 * This class serves as a specialized extension of the [ViewList] class, tailored
 * for handling entities that conform to the `IChangeLog` and `ICommonChangeLog` contracts.
 * It streamlines the process of rendering and interacting with change log records in
 * a tabular format, providing flexible configuration and behavior customization.
 *
 * @param CmnChgLog Type parameter representing the common change log class that extends
 * `ICommonChangeLog`.
 * @param ChgLog Type parameter for the individual change log entries, conforming to `IChangeLog`.
 * @param U Type parameter representing the user entity that extends `IUser`.
 * @param UID Type parameter for the type of identifier used by the user entity.
 * @param configViewList The configuration object for this view, used to define the behavior
 * and settings of the change log list.
 */
abstract class IViewListChangeLog<CmnChgLog : ICommonChangeLog<ChgLog, U, UID>, ChgLog : IChangeLog<U, UID>, U : IUser<UID>, UID : Any>(
    configViewList: ConfigViewList<CmnChgLog, ChgLog, OId<IChangeLog<U, UID>>, *, ChangeLogFilter, Unit, *>,
) : ViewList<CmnChgLog, ChgLog, OId<IChangeLog<U, UID>>, ChangeLogFilter, Unit>(
    configView = configViewList
) {
    companion object {
        /**
         * Initializes a Change Log menu item for the provided view configuration.
         *
         * @param viewListChangeLog The instance of IViewListChangeLog used to configure the change log menu item behavior.
         */
        @Suppress("unused")
        fun initializeChangeLogMenuItem(viewListChangeLog: IViewListChangeLog<*, *, *, *>) {
            ConfigViewItem.contextMenuDefault = {
                if (true) {
                    listOf(
                        menuItem(
                            label = viewListChangeLog.configView.commonContainer.labelList,
                            icon = "fas fa-list-ul",
                        ) { _, _ ->
                            val viewListChangeLog = viewListChangeLog
                            viewListChangeLog.apiFilter = ChangeLogFilter(
                                className = commonContainer.itemKClass.simpleName,
                                serializedId = serializedId,
                            )
                            val modal = Modal(
                                caption = viewListChangeLog.configView.commonContainer.labelList,
                                size = ModalSize.XLARGE
                            ) {
                                span(content = labelItemId)
                                addPageListBody(viewListChangeLog)
                                textAreaInput(rows = 10) {
                                    readonly = true
                                    viewListChangeLog.selectedItemObs.subscribe {
                                        val x = when (it?.action) {
                                            IChangeLog.Action.Create,
                                            IChangeLog.Action.Delete,
                                                -> {
                                                val json = json()
                                                it.data.forEach { (key, value) -> json.add(json(key to value.first)) }
                                                json
                                            }

                                            IChangeLog.Action.Update -> {
                                                val json = json()
                                                it.data.forEach { (key, value) ->
                                                    json.add(
                                                        json(
                                                            key to json(
                                                                "new" to value.first,
                                                                "old" to value.second
                                                            )
                                                        )
                                                    )
                                                }
                                                json
                                            }

                                            null -> null
                                        }
                                        value = JSON.stringify(x, null, "\t")
                                    }
                                }
                            }
                            modal.show()
                        }
                    )
                } else null
            }
        }
    }

    override fun columnDefinitionList(): List<ColumnDefinition<ChgLog>> = listOf(
        ColumnDefinition(
            title = "Action",
            field = fieldName(IChangeLog<*, *>::action),
            hozAlign = Align.CENTER,
            formatterFunction = { cell, _, _ ->
                cell.getDataValue<String?>(IChangeLog<*, *>::action)?.let {
                    createHTML().span {
                        style = "font-weight: bolder"
                        +"${IChangeLog.Action.entries.find(it)?.name}"
                    }
                }
            }
        ),
        ColumnDefinition(
            title = "Fecha",
            field = fieldName(IChangeLog<*, *>::dateTime),
            formatterFunction = { cell, _, _ -> cell.getDataDate(IChangeLog<*, *>::dateTime)?.toDateTimeString }
        ),
        ColumnDefinition(
            title = "UserId",
            field = fieldName(IChangeLog<*, *>::userId),
            hozAlign = Align.CENTER
        ),
        ColumnDefinition(
            title = "User Info",
            field = fieldName(IChangeLog<*, *>::userInfo),
        ),
        ColumnDefinition(
            title = "Data",
            field = fieldName(IChangeLog<*, *>::data),
            width = "20rem",
            formatterFunction = { cell, _, _ -> JSON.stringify(cell.getValue()) }
        ),
        ColumnDefinition(
            title = "Client Info",
            field = fieldName(IChangeLog<*, *>::clientInfo),
            width = "20rem",
        )
    )

    override fun Container.pageListBody() {
        fsTabulator(
            viewList = this@IViewListChangeLog,
            tabulatorOptions = defaultTabulatorOptions().copy(
                height = "calc(100vh - 50vh",
            )
        )
    }
}