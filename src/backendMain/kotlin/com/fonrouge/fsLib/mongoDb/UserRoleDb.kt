package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.model.base.AppUser
import com.fonrouge.fsLib.model.base.UserRole
import kotlinx.coroutines.runBlocking

var UserRoleDb: CTableDb<UserRole, String> = object : CTableDb<UserRole, String>(
    klass = UserRole::class,
    debug = true
) {
    override val lookupFun = {
        listOf(
            LookupBuilder(
                cTableDb = AppUserDb::class,
                localToForeign = UserRole::user_id localToForeign AppUser::_id,
                resultProperty = UserRole::user,
            ),
            LookupBuilder(
                cTableDb = AppRoleDb::class,
                localToForeign = UserRole::appRole_id localToForeign AppRole::_id,
                resultProperty = UserRole::appRole,
            )
        )
    }

    init {
        runBlocking {
            coroutineColl.ensureUniqueIndex(
                UserRole::user_id, UserRole::appRole_id
            )
        }
    }
}
