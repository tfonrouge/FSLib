package com.fonrouge.androidlib

import com.fonrouge.fsLib.model.base.BaseDoc
import kotlinx.serialization.Serializable

@Serializable
data class AndroidClass(
    override val _id: String,
    val name: String,
    val yob: Int,
) : BaseDoc<String> {
    val age: Int = 0
}
