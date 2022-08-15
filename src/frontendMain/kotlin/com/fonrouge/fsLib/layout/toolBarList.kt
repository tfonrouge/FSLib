package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.model.CrudAction
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.TooltipOptions
import io.kvision.core.enableTooltip
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.nav
import io.kvision.navbar.navLink

fun <T : BaseModel<U>, U> Container.toolBarList(
    viewList: ViewList<T, *, U>,
    minToolbarSize: Boolean = true,
): NavbarTabulator<U> {

    val delay = 300

    return navbarTabulator(expand = NavbarExpand.ALWAYS, collapseOnClick = true) {
        nav {
            if (viewList.editable) {
                viewList.configViewItem?.let { configViewItem ->
                    navLink(
                        label = if (minToolbarSize) "" else "Ver detalle",
                        icon = "fas fa-eye"
                    ) {
                        id = CrudAction.Delete.name
                        enableTooltip(TooltipOptions(configViewItem.labelDetail, animation = true, delay = delay))
                        onClick {
                            viewList.crudActionMap[CrudAction.Read]?.invoke(itemId)
                        }
                    }
                    navLink(
                        label = if (minToolbarSize) "" else "Agregar",
                        icon = "fas fa-plus",
                    ) {
                        id = CrudAction.Create.name
                        enableTooltip(TooltipOptions(configViewItem.labelCreate, animation = true, delay = delay))
                        onClick {
                            viewList.crudActionMap[CrudAction.Create]?.invoke(itemId)
                        }
                    }
                    navLink(
                        label = if (minToolbarSize) "" else "Modificar",
                        icon = "fas fa-edit"
                    ) {
                        id = CrudAction.Update.name
                        enableTooltip(TooltipOptions(configViewItem.labelUpdate, animation = true, delay = delay))
                        onClick {
                            viewList.crudActionMap[CrudAction.Update]?.invoke(itemId)
                        }
                    }
                    navLink(
                        label = if (minToolbarSize) "" else "Eliminar",
                        icon = "fas fa-trash-alt"
                    ) {
                        id = CrudAction.Delete.name
                        enableTooltip(TooltipOptions(configViewItem.labelDelete, animation = true, delay = delay))
                        onClick {
                            viewList.crudActionMap[CrudAction.Delete]?.invoke(itemId)
                        }
                    }
                }
                navLink(label = "", icon = "fas fa-ellipsis-v")
                navLink(label = "", icon = "fas fa-clock").onClick {

                }
                navLink(if (minToolbarSize) "" else "Refresh", icon = "fas fa-redo").onClick {
                    viewList.refreshList()
                }
            }
        }
    }
}
