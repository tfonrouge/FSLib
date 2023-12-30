package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.ApiFilter
import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.serializers.OId

val AppRoleColl: Coll<AppRole, OId<AppRole>, ApiFilter> = object : Coll<AppRole, OId<AppRole>, ApiFilter>(
    klass = AppRole::class
) {}
