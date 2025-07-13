package com.fonrouge.fsLib.tabulator

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc

/**
 * Creates a Tabulator menu item based on the provided configuration and API item.
 *
 * @param configViewItem Configuration view item containing label and URL details.
 * @param apiItem API item providing context for the menu item creation.
 * @param icon Optional icon for the menu item.
 * @param disabled Optional flag indicating if the menu item is disabled.
 * @return A TabulatorMenuItem constructed using the provided inputs.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> menuItem(
    configViewItem: ConfigViewItem<CC, T, ID, *, FILT, *>,
    apiItem: ApiItem<T, ID, FILT>,
    icon: String? = null,
    disabled: Boolean? = null,
): TabulatorMenuItem = menuItem(
    label = configViewItem.label,
    icon = icon,
    disabled = disabled,
    url = configViewItem.viewItemUrl(
        apiItem = apiItem
    )
)

/**
 * Creates a TabulatorMenuItem using the specified configuration and parameters.
 *
 * @param configViewList The configuration for the view list, which provides metadata and functionality for managing
 * a collection of items. It must extend ConfigViewList with a compatible container, type, ID, view, filter, and metadata specifications.
 * @param apiFilter An optional API filter used to generate the URL for the menu item. Defaults to an instance of the filter
 * retrieved from the `commonContainer` of the provided `configViewList`.
 * @param icon An optional string representing the icon for the menu item. If null, no icon will be used.
 * @param disabled An optional boolean indicating whether the menu item should be disabled. If null, the default state is applied.
 * @return A TabulatorMenuItem configured with the provided parameters, including label, icon, disabled state, and URL.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> menuItem(
    configViewList: ConfigViewList<CC, T, ID, *, FILT, *, *>,
    apiFilter: FILT = configViewList.commonContainer.apiFilterInstance(),
    icon: String? = null,
    disabled: Boolean? = null,
): TabulatorMenuItem = menuItem(
    label = configViewList.label,
    icon = icon,
    disabled = disabled,
    url = configViewList.viewListUrl(
        apiFilter = apiFilter
    )
)
