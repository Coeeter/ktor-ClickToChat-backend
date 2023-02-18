package com.nasportfolio.routes.users.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePasswordRequest(
    val password: String,
    val oldPassword: String,
)