package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.model.base.ISysUser
import com.fonrouge.fsLib.model.base.SysUser2.Companion.sysUsersCollectionName
import com.fonrouge.fsLib.model.base.SysUserRole
import kotlinx.coroutines.runBlocking
import org.bson.conversions.Bson

var SysUserRoleDb: CTableDb<SysUserRole, String> = object : CTableDb<SysUserRole, String>(
    klass = SysUserRole::class,
    debug = true
) {
    override fun buildPipeline(pipeline: MutableList<Bson>, modelLookup: Array<out ModelLookup<*, *>>): List<Bson> {
        pipeline.addAll(
            listOf(
                lookup5(
                    from = sysUsersCollectionName,
                    localField = SysUserRole::sysUser_id.name,
                    foreignField = ISysUser::_id.name,
                    newAs = SysUserRole::sysUser.name
                ),
                lookup5(
                    from = AppRoleDb.collectionName,
                    localField = SysUserRole::appRole_id.name,
                    foreignField = AppRole::_id.name,
                    newAs = SysUserRole::appRole.name
                )
            )
        )
        pipeline.addAll(buildLookupList(*modelLookup))
        return pipeline
    }

    init {
        runBlocking {
            coroutineColl.ensureUniqueIndex(
                SysUserRole::sysUser_id, SysUserRole::appRole_id
            )
        }
    }
}
