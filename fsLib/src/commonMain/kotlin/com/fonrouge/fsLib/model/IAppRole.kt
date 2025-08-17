package com.fonrouge.fsLib.model

import com.fonrouge.fsLib.enums.XEnum
import com.fonrouge.fsLib.api.CrudTask
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Interface representing an application role with specific properties and permissions.
 *
 * @param ID The type of the identifier.
 */
@Suppress("unused")
interface IAppRole<ID : Any> : BaseDoc<ID> {
    val classOwner: String
    val funcName: String?
    val roleType: RoleType
    val description: String
    val detail: String?
    val defaultPermission: BaseRolePermission
    val defaultCrudTaskSet: Set<CrudTask>?
    val upVoteInGroup: BaseRolePermission

    @Serializable
    enum class RoleType(override val encoded: String) : XEnum {
        @SerialName("S")
        SingleAction("S"),

        @SerialName("CT")
        CrudTask("CT"),
    }

    @Serializable
    enum class BaseRolePermission(override val encoded: String) : XEnum {
        @SerialName("1")
        Allow("1"),

        @SerialName("0")
        Deny("0"),
    }
}
