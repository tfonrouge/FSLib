package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.SysUser

var SysUserDb: CTableDb<SysUser, String> = object : CTableDb<SysUser, String>(
    klass = SysUser::class
) {}
