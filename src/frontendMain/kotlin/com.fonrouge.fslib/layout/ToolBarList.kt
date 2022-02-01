package com.fonrouge.fslib.layout

import com.fonrouge.fslib.lib.ActionParam
import com.fonrouge.fslib.model.base.BaseContainerList
import com.fonrouge.fslib.model.base.BaseModel
import com.fonrouge.fslib.view.ViewDataContainer.Companion.clearHandleIntervalStack
import com.fonrouge.fslib.view.ViewList
import io.kvision.core.Container
import io.kvision.core.TooltipOptions
import io.kvision.core.enableTooltip
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.nav
import io.kvision.navbar.navLink
import io.kvision.routing.routing

fun <T : BaseModel<*>, V : BaseContainerList<T>> Container.toolBarList(
    viewList: ViewList<T, V>,
    minToolbarSize: Boolean = true,
): NavbarTabulator {

    val delay = 300

    return navbarTabulator(expand = NavbarExpand.ALWAYS, collapseOnClick = true) {
        nav {
            if (viewList.editable) {
                navLink(
                    label = if (minToolbarSize) "" else "Agregar",
                    icon = "fas fa-plus",
                ) {
                    id = ActionParam.Insert.name
                    enableTooltip(TooltipOptions(viewList.configViewItem.labelInsert, animation = true, delay = delay))
                    onClick {
                        viewList.actionParamMap[ActionParam.Insert]?.invoke(item, null)
                    }
                }
                navLink(
                    label = if (minToolbarSize) "" else "Modificar",
                    icon = "fas fa-edit"
                ) {
                    id = ActionParam.Update.name
                    enableTooltip(TooltipOptions(viewList.configViewItem.labelUpdate, animation = true, delay = delay))
                    onClick {
                        viewList.actionParamMap[ActionParam.Update]?.invoke(item, null)
                    }
                }
                navLink(
                    label = if (minToolbarSize) "" else "Eliminar",
                    icon = "fas fa-trash-alt"
                ) {
                    id = ActionParam.Delete.name
                    enableTooltip(TooltipOptions(viewList.configViewItem.labelDelete, animation = true, delay = delay))
                    onClick {
                        viewList.actionParamMap[ActionParam.Delete]?.invoke(item, null)
                    }
                }
                navLink(
                    label = if (minToolbarSize) "" else "Ver detalle",
                    icon = "fas fa-eye"
                ) {
                    id = ActionParam.Delete.name
                    enableTooltip(TooltipOptions(viewList.configViewItem.labelDetail, animation = true, delay = delay))
                    onClick {
                        val url = item?.id?.let { "/${viewList.configViewItem.url}?id=${it}" }
                        url?.let { it1 -> routing.navigate(it1) }
                    }
                }
                navLink(label = "", icon = "fas fa-ellipsis-v")
                navLink(label = "", icon = "fas fa-clock").onClick {
                    clearHandleIntervalStack()
                }
                navLink(if (minToolbarSize) "" else "Refresh", icon = "fas fa-redo").onClick {
                    viewList.refreshList()
                }
            }
/*
            dropDown(
                "Favourites",
                listOf("HTML" to "#!/basic", "Forms" to "#!/forms"),
                icon = "fas fa-star",
                forNavbar = true
            )
*/
        }

        /*
            navForm {
                text(label = "Search:")
            }
    */
/*
        nav(rightAlign = true) {
            navLink("System", icon = "fab fa-windows")
        }
*/
    }
}
