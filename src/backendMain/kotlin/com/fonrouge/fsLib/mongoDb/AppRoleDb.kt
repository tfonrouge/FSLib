package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.serializers.OId

val AppRoleDb: Coll<AppRole, OId<AppRole>, Unit> = object : Coll<AppRole, OId<AppRole>, Unit>(
    klass = AppRole::class
) {}
