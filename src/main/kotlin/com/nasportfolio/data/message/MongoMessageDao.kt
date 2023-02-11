package com.nasportfolio.data.message

import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineDatabase

class MongoMessageDao(
    db: CoroutineDatabase
) : MessageDao {
    private val collection = db.getCollection<Message>()

    override suspend fun getAllMessagesOfChat(participants: List<String>): List<Message> {
        val query = and(
            Message::senderId `in` participants,
            Message::receiverId `in` participants
        )
        return collection.find(query)
            .descendingSort(Message::createdAtTimestamp)
            .toList()
    }

    override suspend fun getAllMessagesOfUser(userId: String): List<Message> {
        val query = or(
            Message::receiverId eq userId,
            Message::senderId eq userId
        )
        return collection.find(query)
            .descendingSort(Message::createdAtTimestamp)
            .toList()
    }

    override suspend fun getMessageInIdList(messageIdList: List<String>): List<Message> {
        val query = Message::id `in` messageIdList.map { ObjectId(it) }
        return collection.find(query)
            .descendingSort(Message::createdAtTimestamp)
            .toList()
    }

    override suspend fun getMessageById(messageId: String): Message? {
        return collection.findOne(Message::id eq ObjectId(messageId))
    }

    override suspend fun insertMessage(message: Message): Boolean {
        return collection.insertOne(message).wasAcknowledged()
    }

    override suspend fun updateMessage(message: Message): Boolean {
        return collection.updateOne(Message::id eq message.id, message)
            .wasAcknowledged()
    }

    override suspend fun setMultipleMessagesSeen(messageIdList: List<String>): Boolean {
        val query = Message::id `in` messageIdList.map { ObjectId(it) }
        val updates = SetTo(Message::seen, true)
        return collection.updateMany(query, updates)
            .wasAcknowledged()
    }
}