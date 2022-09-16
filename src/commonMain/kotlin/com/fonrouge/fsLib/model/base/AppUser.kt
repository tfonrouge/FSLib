package com.fonrouge.fsLib.model.base

import kotlinx.serialization.Serializable

@Serializable
class AppUser(
    override val _id: String,
    override var code: String,
    override var name: String,
    override var rootUser: Boolean = false,
    override var password: String
) : IAppUser
