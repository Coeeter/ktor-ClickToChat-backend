package com.nasportfolio.data.user

import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    @BsonId val id: ObjectId = ObjectId(),
    val username: String,
    val email: String,
    val password: String,
    val createdAtTimeStamp: Long,
    val fcmToken: String? = null,
    val imageUrl: String? = null,
) : Principal

val ApplicationCall.user get() = authentication.principal<User>()
