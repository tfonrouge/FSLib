package com.fonrouge.backendLib.model

import com.fonrouge.base.enums.XEnum
import com.fonrouge.base.model.BaseDoc
import com.fonrouge.base.model.IUser
import com.fonrouge.base.types.OId
import io.kvision.types.OffsetDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface IChangeLog<U : IUser<UID>, UID : Any> : BaseDoc<OId<IChangeLog<U, UID>>> {
    abstract override val _id: OId<IChangeLog<U, UID>>
    val className: String
    val serializedId: String
    val dateTime: OffsetDateTime
    val action: Action
    val clientInfo: String?
    val userId: UID?
    val userInfo: String?
    val data: Map<String, Pair<String?, String?>>

    @Serializable
    enum class Action(override val encoded: String) : XEnum {
        @SerialName("C")
        Create("C"),

        @SerialName("U")
        Update("U"),

        @SerialName("D")
        Delete("D"),
    }
}