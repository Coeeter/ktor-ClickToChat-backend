package com.nasportfolio.routes.messages.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMessageRequest(
    val receiverId: String,
    val messageId: String? = null,
    val message: String? = null,
)