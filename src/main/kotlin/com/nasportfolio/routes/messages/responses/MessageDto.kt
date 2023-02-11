package com.nasportfolio.routes.messages.responses

import com.nasportfolio.data.message.Message
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val createdAtTimestamp: Long,
    val updatedAtTimestamp: Long,
    val imageUrl: String?,
    val type: MessageType
)

enum class MessageType { CREATE, UPDATE, GET }

fun Message.toMessageDto(type: MessageType): MessageDto {
    return MessageDto(
        id = id.toString(),
        senderId = senderId,
        receiverId = receiverId,
        message = message,
        createdAtTimestamp = createdAtTimestamp,
        updatedAtTimestamp = updatedAtTimestamp,
        imageUrl = imageUrl,
        type = type
    )
}