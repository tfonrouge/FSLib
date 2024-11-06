package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.config.ConfigViewItem
import com.fonrouge.fsLib.config.ConfigViewList
import com.fonrouge.fsLib.config.ICommonContainer
import com.fonrouge.fsLib.model.apiData.ApiItem
import com.fonrouge.fsLib.model.apiData.IApiFilter
import com.fonrouge.fsLib.model.base.BaseDoc
import io.kvision.dropdown.DropDown
import io.kvision.dropdown.ddLink

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> DropDown.ddLink(
    configViewItem: ConfigViewItem<CC, T, ID, *, *, FILT>,
    apiItem: ApiItem<T, ID, FILT>,
) {
    ddLink(
        label = configViewItem.label,
        url = configViewItem.viewItemUrl(
            apiItem = apiItem
        )
    )
}

@Suppress("unused")
fun <CC : ICommonContainer<T, ID, FILT>, T : BaseDoc<ID>, ID : Any, FILT : IApiFilter<*>> DropDown.ddLink(
    configViewList: ConfigViewList<CC, T, ID, *, *, FILT, *>,
    apiFilter: FILT = configViewList.commonContainer.apiFilterInstance(),
) {
    ddLink(
        label = configViewList.label,
        url = configViewList.viewListUrl(
            apiFilter = apiFilter
        )
    )
}
