package com.nasportfolio.routes.messages.responses

import com.nasportfolio.routes.messages.requests.SocketRequestType
import kotlinx.serialization.Serializable

@Serializable
data class SocketResponse(
    val type: SocketResponseType,
    val message: MessageDto? = null,
    val senderId: String? = null,
)

enum class SocketResponseType {
    CREATE_MESSAGE, UPDATE_MESSAGE, USER_TYPING, USER_STOP_TYPING
}