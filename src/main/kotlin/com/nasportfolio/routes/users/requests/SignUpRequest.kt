package com.nasportfolio.routes.users.requests

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String,
    val fcmToken: String? = null,
)