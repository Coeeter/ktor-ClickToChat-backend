package com.nasportfolio.routes.users.responses

import com.nasportfolio.data.user.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val createdAtTimestamp: Long,
    val imageUrl: String? = null,
)

fun User.toUserDto(): UserDto {
    return UserDto(
        username = username,
        email = email,
        createdAtTimestamp = createdAtTimeStamp,
        id = id.toHexString(),
        imageUrl = imageUrl
    )
}