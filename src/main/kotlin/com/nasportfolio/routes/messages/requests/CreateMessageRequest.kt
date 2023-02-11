package com.nasportfolio.routes.messages.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateMessageRequest(
    val receiverId: String,
    val message: String,
    val imageUrl: String? = null
)