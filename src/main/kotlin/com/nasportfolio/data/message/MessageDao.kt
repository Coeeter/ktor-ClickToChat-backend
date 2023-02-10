package com.nasportfolio.data.message

interface MessageDao {
    suspend fun getAllMessagesOfChat(senderId: String, receiverId: String): List<Message>
    suspend fun insertMessage(message: Message): Boolean
    suspend fun updateMessage(message: Message): Boolean
    suspend fun deleteMessage(message: Message): Boolean
}