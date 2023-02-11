package com.nasportfolio.routes.messages.requests

import kotlinx.serialization.Serializable

@Serializable
data class SocketRequest(
    val type: SocketRequestType,
    val createMessageRequest: CreateMessageRequest? = null,
    val updateMessageRequest: UpdateMessageRequest? = null
)

enum class SocketRequestType {
    CREATE, UPDATE
}