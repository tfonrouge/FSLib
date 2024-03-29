package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.layout.TabulatorMenuItem
import com.fonrouge.fsLib.layout.menuItem
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT, *>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> MutableList<TabulatorMenuItem>.menuItem(
    configViewItem: ConfigViewItem<CC, T, ID, *, *, FILT>,
    apiItem: ApiItem.Query<T, ID, FILT>,
) {
    menuItem(
        label = configViewItem.label,
        url = urlFromApiItem(
            configViewItem = configViewItem,
            apiItem = apiItem
        )
    )
}

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT, *>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> MutableList<TabulatorMenuItem>.menuItem(
    configViewList: ConfigViewList<CC, T, ID, *, *, FILT>,
    apiFilter: FILT = configViewList.commonContainer.apiFilterInstance(),
) {
    menuItem(
        label = configViewList.label,
        url = urlApiFilter(
            configView = configViewList,
            apiFilter = apiFilter
        )
    )
}
