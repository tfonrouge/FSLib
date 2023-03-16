package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.serializers.OId

val AppRoleDb: CTableDb<AppRole, OId<AppRole>> = object : CTableDb<AppRole, OId<AppRole>>(
    klass = AppRole::class
) {}
