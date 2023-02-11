package com.nasportfolio.routes.messages.responses

import kotlinx.serialization.Serializable

@Serializable
data class SocketResponse(
    val type: SocketResponseType,
    val message: MessageDto? = null,
    val senderId: String? = null,
    val messagesSeen: List<MessageDto>? = null
)

enum class SocketResponseType {
    CREATE_MESSAGE,
    UPDATE_MESSAGE,
    USER_TYPING,
    USER_STOP_TYPING,
    SEEN_MESSAGES,
}