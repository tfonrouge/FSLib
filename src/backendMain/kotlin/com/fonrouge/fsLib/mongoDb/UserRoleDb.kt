package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.model.base.ISysUser
import com.fonrouge.fsLib.model.base.SysUserRole
import kotlinx.coroutines.runBlocking

var SysUserRoleDb: CTableDb<SysUserRole, String> = object : CTableDb<SysUserRole, String>(
    klass = SysUserRole::class,
    debug = true
) {
    override val lookupFun = {
        listOf(
            lookupField(
                cTableDb = SysUserDb::class,
                localField = SysUserRole::sysUser_id,
                foreignField = ISysUser::_id,
                resultField = SysUserRole::sysUser,
            ),
            lookupField(
                cTableDb = AppRoleDb::class,
                localField = SysUserRole::appRole_id,
                foreignField = AppRole::_id,
                resultField = SysUserRole::appRole,
            )
        )
    }

    init {
        runBlocking {
            coroutineColl.ensureUniqueIndex(
                SysUserRole::sysUser_id, SysUserRole::appRole_id
            )
        }
    }
}
