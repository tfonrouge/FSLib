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
            LookupBuilder(
                cTableDb = AppUserDb::class,
                localToForeign = AppUserRole::appUser_id localToForeign AppUser::_id,
                resultProperty = AppUserRole::appUser,
            ),
            LookupBuilder(
                cTableDb = AppRoleDb::class,
                localToForeign = AppUserRole::appRole_id localToForeign AppRole::_id,
                resultProperty = AppUserRole::appRole,
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
