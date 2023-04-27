package com.fonrouge.fsLib.model.base

import com.fonrouge.fsLib.serializers.OId
import kotlinx.serialization.Serializable

@Serializable
data class SysUser(
    override val _id: OId<ISysUser>,
//    override var name: String,
    override var rootUser: Boolean = false,
//    override var password: String
) : ISysUser {
    companion object {
        var sysUsersCollectionName = "__sysUsers"
    }
}
