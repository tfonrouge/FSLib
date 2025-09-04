package com.fonrouge.shopify.model

import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.types.OId

abstract class IProductFilter : IApiFilter<OId<IStore>>()
