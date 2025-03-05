package com.fonrouge.fsLib.tabulator

import com.fonrouge.fsLib.common.ICommonContainer
import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc

/**
 * Adds a `TabulatorMenuItem` to the mutable list with the specified configuration and API item.
 *
 * @param configViewItem The configuration view item that provides the label and URL setup for the menu item.
 * @param apiItem The API item representing the backend object, used for constructing the menu item URL.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> MutableList<TabulatorMenuItem>.menuItem(
    configViewItem: ConfigViewItem<CC, T, ID, *, FILT, *>,
    apiItem: ApiItem<T, ID, FILT>,
) {
    menuItem(
        label = configViewItem.label,
        url = configViewItem.viewItemUrl(
            apiItem = apiItem
        )
    )
}

/**
 * Adds a menu item to a list of Tabulator menu items, with configurations derived from a `ConfigViewList`.
 *
 * @param CC A type parameter representing the common container, which must extend `ICommonContainer`.
 * @param T A type parameter representing the base document, which must extend `BaseDoc`.
 * @param ID The type parameter representing the ID of the document, which must be non-nullable.
 * @param FILT A type parameter representing the API filter, which must extend `IApiFilter`.
 * @param configViewList The configuration object that provides details for the view list, including label and URL generation.
 * @param apiFilter An optional API filter instance. If not provided, it defaults to the API filter instance retrieved from the `ConfigViewList`.
 */
@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> MutableList<TabulatorMenuItem>.menuItem(
    configViewList: ConfigViewList<CC, T, ID, *, *, FILT, *>,
    apiFilter: FILT = configViewList.commonContainer.apiFilterInstance(),
) {
    menuItem(
        label = configViewList.label,
        url = configViewList.viewListUrl(
            apiFilter = apiFilter
        )
    )
}
