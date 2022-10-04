package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.AppScope
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.TooltipOptions
import io.kvision.core.enableTooltip
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.nav
import io.kvision.navbar.navLink
import kotlinx.coroutines.launch

fun <T : BaseModel<U>, U> Container.toolBarList(
    viewList: ViewList<T, *, U>,
    minToolbarSize: Boolean = true,
): NavbarTabulator<U> {

    val delay = 300

    return navbarTabulator(expand = NavbarExpand.ALWAYS, collapseOnClick = true) {
        nav {
            if (viewList.editable) {
                viewList.configViewItem?.let { configViewItem ->
                    linkRead = navLink(
                        label = if (minToolbarSize) "" else "Detail",
                        icon = viewList.iconCrud(CrudAction.Read),
                    ) {
                        enableTooltip(TooltipOptions(configViewItem.labelDetail, animation = true, delay = delay))
                    }
                    navLink(
                        label = if (minToolbarSize) "" else "Create",
                        icon = viewList.iconCrud(CrudAction.Create),
                        url = viewList.actionUrl(CrudAction.Create, null)
                    ) {
                        enableTooltip(TooltipOptions(configViewItem.labelCreate, animation = true, delay = delay))
                    }
                    linkUpdate = navLink(
                        label = if (minToolbarSize) "" else "Update",
                        icon = viewList.iconCrud(CrudAction.Update)
                    ) {
                        enableTooltip(TooltipOptions(configViewItem.labelUpdate, animation = true, delay = delay))
                    }
                    linkDelete = navLink(
                        label = if (minToolbarSize) "" else "Delete",
                        icon = viewList.iconCrud(CrudAction.Delete)
                    ) {
                        enableTooltip(TooltipOptions(configViewItem.labelDelete, animation = true, delay = delay))
                    }
                }
                navLink(label = "", icon = "fas fa-ellipsis-v")
                navLink(label = "", icon = "fas fa-clock", url = "JuanaLaCubana")
                navLink(if (minToolbarSize) "" else "Refresh", icon = "fas fa-redo").onClick {
                    AppScope.launch {
                        viewList.dataUpdate()
                    }
                }
            }
        }
    }
}
