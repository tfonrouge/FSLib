package com.fonrouge.fsLib.dataModel

import com.fonrouge.fsLib.mongoDb.connectionString
import com.fonrouge.fsLib.mongoDb.mongoDatabase
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.coroutine
import java.time.LocalDateTime
import java.util.*

@Serializable
data class UserProfile(
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

val userProfileColl by lazy {
    println("${Date()} mongoClient with $connectionString")
    mongoDatabase.getCollection("users", UserProfile::class.java).coroutine
}
