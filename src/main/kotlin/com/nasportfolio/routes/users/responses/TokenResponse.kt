package com.nasportfolio.routes.users.responses

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val token: String
)