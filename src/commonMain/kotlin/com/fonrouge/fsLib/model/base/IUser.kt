package com.fonrouge.fsLib.model.base

interface IUser<UID : Any> : BaseDoc<UID> {
    override val _id: UID
    val rootUser: Boolean
}
