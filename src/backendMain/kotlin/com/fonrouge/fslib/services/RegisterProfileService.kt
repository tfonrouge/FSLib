package com.fonrouge.fslib.services

import org.apache.commons.codec.digest.DigestUtils
import org.bson.types.ObjectId

actual class RegisterProfileService : IRegisterProfileService {

    override suspend fun registerProfile(profile: Profile, password: String): Boolean {
        try {
            val user = User(
                _id = ObjectId(),
                enabled = true,
                password2 = DigestUtils.sha256Hex(password),
                userLevel = "",
                userName = profile.username ?: "",
                name = profile.name ?: ""
            )
            userColl.insertOne(user)
        } catch (e: Exception) {
            throw Exception("Register operation failed!")
        }
        return true
    }
}
