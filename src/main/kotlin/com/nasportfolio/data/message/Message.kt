package com.nasportfolio.data.message

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Message(
    val senderId: String,
    val receiverId: String,
    val message: String,
    val createdAtTimestamp: Long,
    val updatedAtTimestamp: Long,
    val imageUrl: String? = null,
    val seen: Boolean = false,
    @BsonId val id: ObjectId = ObjectId()
)