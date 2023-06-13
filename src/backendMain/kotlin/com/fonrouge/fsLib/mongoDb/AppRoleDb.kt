package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.serializers.OId

val AppRoleDb: Coll<AppRole, OId<AppRole>> = object : Coll<AppRole, OId<AppRole>>(
    klass = AppRole::class
) {}
