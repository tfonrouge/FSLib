package com.fonrouge.fullStack.tabulator

import com.fonrouge.base.api.ApiItem
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.fullStack.config.ConfigViewItem
import com.fonrouge.fullStack.config.ConfigViewList

/**
 * Creates a TabulatorMenuItem using the provided configuration, API item, and optional parameters.
 *
 * @param CC The type of the common container, which manages API items and extends ICommonContainer.
 * @param T The type of the item managed by the container, which must extend BaseDoc.
 * @param ID The type of the ID field of the items, which must be a non-nullable type.
 * @param FILT The type of the filter used for querying, must extend IApiFilter.
 * @param configViewItem The configuration object for the view item that provides information such as labels and URLs.
 * @param label The label for the menu item. Defaults to the label from the provided ConfigViewItem.
 * @param apiItem The API item associated with the menu item.
 * @param icon An optional string representing the icon for the menu item. Defaults to null.
 * @param disabled An optional boolean indicating whether the menu item is disabled. Defaults to null.
 * @return A TabulatorMenuItem constructed with the provided parameters.
 */
@Suppress("unused")
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> menuItem(
    configViewItem: ConfigViewItem<T, ID, *, FILT, *>,
    label: String = configViewItem.label,
    apiItem: ApiItem<T, ID, FILT>,
    icon: String? = null,
    disabled: Boolean? = null,
): TabulatorMenuItem = menuItem(
    label = label,
    icon = icon,
    disabled = disabled,
    url = configViewItem.apiItemToUrlString(
        apiItem = apiItem
    )
)

/**
 * Creates a menu item for a Tabulator-based UI system, configured based on the provided view list configuration.
 *
 * @param CC Type of the common container managing API items, extends ICommonContainer.
 * @param T Type of items within the container, extends BaseDoc.
 * @param ID Type of the item identifier, must be non-nullable.
 * @param FILT Type of the API filter, extends IApiFilter.
 * @param configViewList The configuration of the view list, which provides necessary information for generating menu items.
 * @param label The label for the menu item. Defaults to the label from the provided configViewList.
 * @param apiFilter The API filter for the menu item's associated view. Defaults to the filter from the provided configViewList's container.
 * @param icon The optional icon for the menu item. Defaults to null.
 * @param disabled Whether the menu item is disabled. Defaults to null.
 * @return A TabulatorMenuItem configured with the specified options.
 */
@Suppress("unused")
fun <T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> menuItem(
    configViewList: ConfigViewList<T, ID, *, FILT, *, *>,
    label: String = configViewList.label,
    apiFilter: FILT = configViewList.commonContainer.apiFilterInstance(),
    icon: String? = null,
    disabled: Boolean? = null,
): TabulatorMenuItem = menuItem(
    label = label,
    icon = icon,
    disabled = disabled,
    url = configViewList.viewUrl(
        apiFilter = apiFilter
    )
)
