package com.nasportfolio.data.message

import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoMessageDao(
    db: CoroutineDatabase
) : MessageDao {
    private val collection = db.getCollection<Message>()

    override suspend fun getAllMessagesOfChat(senderId: String, receiverId: String): List<Message> {
        val query = and(
            Message::senderId eq senderId,
            Message::receiverId eq receiverId
        )
        return collection.find(query)
            .descendingSort(Message::updatedAtTimestamp)
            .toList()
    }

    override suspend fun insertMessage(message: Message): Boolean {
        return collection.insertOne(message).wasAcknowledged()
    }

    override suspend fun updateMessage(message: Message): Boolean {
        return collection.updateOne(Message::id eq message.id, message)
            .wasAcknowledged()
    }

    override suspend fun deleteMessage(message: Message): Boolean {
        return collection.deleteOne(Message::id eq message.id)
            .wasAcknowledged()
    }
}