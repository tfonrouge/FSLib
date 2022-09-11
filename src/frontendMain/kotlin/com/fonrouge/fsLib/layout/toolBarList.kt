package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.apiLib.AppScope
import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseModel
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
                        icon = "fas fa-eye",
                    ) {
                        enableTooltip(TooltipOptions(configViewItem.labelDetail, animation = true, delay = delay))
                    }
                    navLink(
                        label = if (minToolbarSize) "" else "Create",
                        icon = "fas fa-plus",
                        url = viewList.actionUrl(CrudAction.Create, null)
                    ) {
                        enableTooltip(TooltipOptions(configViewItem.labelCreate, animation = true, delay = delay))
                    }
                    linkUpdate = navLink(
                        label = if (minToolbarSize) "" else "Update",
                        icon = "fas fa-edit"
                    ) {
                        enableTooltip(TooltipOptions(configViewItem.labelUpdate, animation = true, delay = delay))
                    }
                    linkDelete = navLink(
                        label = if (minToolbarSize) "" else "Delete",
                        icon = "fas fa-trash-alt"
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
