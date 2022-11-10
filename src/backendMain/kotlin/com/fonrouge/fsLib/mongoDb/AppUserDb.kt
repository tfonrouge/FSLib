package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.SysUser
import com.mongodb.client.model.IndexOptions
import kotlinx.coroutines.runBlocking

var SysUserDb: CTableDb<SysUser, String> = object : CTableDb<SysUser, String>(
    klass = SysUser::class
) {
    init {
        runBlocking {
            coroutineColl.ensureUniqueIndex(
                SysUser::code,
                indexOptions = IndexOptions()
                    .collation(collation())
            )
        }
    }
}
