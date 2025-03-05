package com.fonrouge.fsLib.commonServices

import kotlinx.serialization.Serializable

/**
 * Represents a user login request with a username and password.
 *
 * This data class is used to encapsulate the credentials of a user attempting to log in.
 *
 * @property username The username of the user.
 * @property password The password of the user.
 */
@Suppress("unused")
@Serializable
data class UserLogin(
    val username: String,
    val password: String,
)
