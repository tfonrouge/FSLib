package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppUser

var AppUserDb: CTableDb<AppUser, String> = object : CTableDb<AppUser, String>(
    klass = AppUser::class
) {
}
