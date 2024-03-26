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
fun <CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> MutableList<TabulatorMenuItem>.menuItem(
    configViewItem: ConfigViewItem<CV, T, ID, *, *, FILT>,
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
fun <CV : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter> MutableList<TabulatorMenuItem>.menuItem(
    configViewList: ConfigViewList<CV, T, ID, *, *, FILT>,
    apiFilter: FILT = configViewList.commonView.apiFilterInstance(),
) {
    menuItem(
        label = configViewList.label,
        url = urlApiFilter(
            configView = configViewList,
            apiFilter = apiFilter
        )
    )
}
