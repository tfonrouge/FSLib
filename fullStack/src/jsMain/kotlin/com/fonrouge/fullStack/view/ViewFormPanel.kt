package com.fonrouge.fullStack.view

import com.fonrouge.base.model.BaseDoc

class ViewFormPanel<K : BaseDoc<*>>(
    viewItem: ViewItem<*, K, *, *>
) : XFormPanel<K>(
    serializer = viewItem.configView.commonContainer.itemSerializer,
    viewItem = viewItem
)
