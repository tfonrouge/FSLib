package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.model.base.AppUser
import com.fonrouge.fsLib.model.base.AppUserRole
import kotlinx.coroutines.runBlocking

var AppUserRoleDb: CTableDb<AppUserRole, String> = object : CTableDb<AppUserRole, String>(
    klass = AppUserRole::class,
    debug = true
) {
    override val lookupFun = {
        listOf(
            lookupField(
                cTableDb = AppUserDb::class,
                localField = AppUserRole::appUser_id,
                foreignField = AppUser::_id,
                resultField = AppUserRole::appUser,
            ),
            lookupField(
                cTableDb = AppRoleDb::class,
                localField = AppUserRole::appRole_id,
                foreignField = AppRole::_id,
                resultField = AppUserRole::appRole,
            )
        )
    }

    init {
        runBlocking {
            coroutineColl.ensureUniqueIndex(
                AppUserRole::appUser_id, AppUserRole::appRole_id
            )
        }
    }
}
