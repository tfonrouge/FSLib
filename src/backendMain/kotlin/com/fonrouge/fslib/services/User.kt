package com.fonrouge.fslib.services

import com.fonrouge.fslib.mongoDb.mongoDatabase
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.coroutine
import java.time.LocalDateTime

@Serializable
data class User(
    @Contextual
    val _id: ObjectId,
    val enabled: Boolean?,
    val password2: String,
    val userLevel: String?,
    val userName: String,
    val name: String,
    @Contextual
    val lastLogin: LocalDateTime? = null,
)

val userColl by lazy {
    mongoDatabase.getCollection("users", User::class.java).coroutine
}
