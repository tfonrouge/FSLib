package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppRole

val AppRoleDb: CTableDb<AppRole, String> = object : CTableDb<AppRole, String>(
    klass = AppRole::class
) {}
