package com.fonrouge.shopify.model

import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.types.LongId

interface IProduct<ID: LongId<*>> : BaseDoc<ID>
