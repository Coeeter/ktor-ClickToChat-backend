package com.nasportfolio.routes.messages.requests

import kotlinx.serialization.Serializable

@Serializable
data class MessageSeenRequest(
    val receiverId: String,
    val messages: List<String>
)