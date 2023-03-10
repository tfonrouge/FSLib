package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.serializers.Id

val AppRoleDb: CTableDb<AppRole, Id<AppRole>> = object : CTableDb<AppRole, Id<AppRole>>(
    klass = AppRole::class
) {}
