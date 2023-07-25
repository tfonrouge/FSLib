package com.fonrouge.fsLib.mongoDb

import com.fonrouge.fsLib.model.apiData.ApiFilter
import com.fonrouge.fsLib.model.base.AppRole
import com.fonrouge.fsLib.model.base.ISysUser
import com.fonrouge.fsLib.model.base.SysUser
import com.fonrouge.fsLib.model.base.SysUserRole
import com.fonrouge.fsLib.serializers.OId
import com.mongodb.client.model.UnwindOptions
import kotlinx.coroutines.runBlocking
import org.bson.conversions.Bson
import org.litote.kmongo.unwind

var SysUserRoleColl: Coll<SysUserRole, OId<SysUserRole>, ApiFilter> =
    object : Coll<SysUserRole, OId<SysUserRole>, ApiFilter>(
        klass = SysUserRole::class,
        debug = true
    ) {
        override fun buildPipeline(
            pipeline: MutableList<Bson>,
            lookupWrappers: Array<out LookupWrapper<*, *>>,
            apiFilter: ApiFilter?,
        ): MutableList<Bson> {
            pipeline.addAll(
                listOf(
                    lookup5(
                        from = SysUser.sysUsersCollectionName,
                        localField = SysUserRole::sysUser_id.name,
                        foreignField = ISysUser::_id.name,
                        newAs = SysUserRole::sysUser.name
                    ),
                    SysUserRole::sysUser.unwind(unwindOptions = UnwindOptions().preserveNullAndEmptyArrays(true)),
                    lookup5(
                        from = AppRoleDb.collectionName,
                        localField = SysUserRole::appRole_id.name,
                        foreignField = AppRole::_id.name,
                        newAs = SysUserRole::appRole.name
                    ),
                    SysUserRole::appRole.unwind(unwindOptions = UnwindOptions().preserveNullAndEmptyArrays(true)),
                )
            )
            pipeline.addAll(buildLookupList(lookupWrappers, apiFilter))
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
