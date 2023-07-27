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
        override fun customPipelineItems(
            pipeline: MutableList<Bson>,
            apiFilter: ApiFilter?,
        ): MutableList<Bson> {
            pipeline.addAll(
                listOf(
                    lookup(
                        from = SysUser.sysUsersCollectionName,
                        localField = SysUserRole::sysUser_id,
                        foreignField = ISysUser::_id,
                        resultProperty = SysUserRole::sysUser
                    ),
                    SysUserRole::sysUser.unwind(unwindOptions = UnwindOptions().preserveNullAndEmptyArrays(true)),
                    lookup(
                        from = AppRoleDb.collectionName,
                        localField = SysUserRole::appRole_id,
                        foreignField = AppRole::_id,
                        resultProperty = SysUserRole::appRole
                    ),
                    SysUserRole::appRole.unwind(unwindOptions = UnwindOptions().preserveNullAndEmptyArrays(true)),
                )
            )
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
