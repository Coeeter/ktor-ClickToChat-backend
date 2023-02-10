package com.nasportfolio.routes.users.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateAccountRequest(
    val username: String? = null,
    val email: String? = null
)