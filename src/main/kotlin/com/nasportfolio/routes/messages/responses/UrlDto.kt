package com.nasportfolio.routes.messages.responses

import kotlinx.serialization.Serializable

@Serializable
data class UrlDto(
    val url: String
)