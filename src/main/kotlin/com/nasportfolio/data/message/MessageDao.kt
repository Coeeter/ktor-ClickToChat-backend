package com.nasportfolio.data.message

interface MessageDao {
    suspend fun getAllMessagesOfChat(senderId: String, receiverId: String): List<Message>
    suspend fun getMessageInIdList(messageIdList: List<String>): List<Message>
    suspend fun getMessageById(messageId: String): Message?
    suspend fun insertMessage(message: Message): Boolean
    suspend fun updateMessage(message: Message): Boolean
}