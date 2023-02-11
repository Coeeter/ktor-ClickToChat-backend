package com.nasportfolio.routes.messages

import com.nasportfolio.data.images.ImageDao
import com.nasportfolio.data.message.Message
import com.nasportfolio.data.message.MessageDao
import com.nasportfolio.data.notifications.NotificationDao
import com.nasportfolio.data.user.UserDao
import com.nasportfolio.routes.messages.exceptions.InvalidKeyException
import com.nasportfolio.routes.messages.exceptions.MessageNotFoundException
import com.nasportfolio.routes.messages.exceptions.UserAlreadyConnectedException
import com.nasportfolio.routes.messages.responses.MessageDto
import com.nasportfolio.routes.messages.responses.SocketResponse
import com.nasportfolio.routes.messages.responses.SocketResponseType
import com.nasportfolio.routes.messages.responses.toMessageDto
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MessageService(
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val notificationDao: NotificationDao,
    private val imageDao: ImageDao
) {
    private val keys = ConcurrentHashMap<String, SocketKey>()
    private val sockets = ConcurrentHashMap<String, WebSocketSession>()

    fun generateKey(userId: String): String {
        keys[userId] = SocketKey(
            id = UUID.randomUUID().toString(),
            expiresAt = System.currentTimeMillis() + 10L * 60L * 1000L
        )
        return keys[userId]!!.id
    }

    fun onJoin(
        userId: String,
        key: String,
        socket: WebSocketSession
    ) {
        if (sockets.containsKey(userId)) throw UserAlreadyConnectedException()
        if (keys[userId]?.id != key) throw InvalidKeyException()
        if (System.currentTimeMillis() >= keys[userId]!!.expiresAt) {
            keys.remove(userId)
            throw InvalidKeyException()
        }
        keys.remove(userId)
        sockets[userId] = socket
    }

    suspend fun getMessagesOfChat(
        userId: String,
        receiverId: String
    ): List<MessageDto> {
        return messageDao.getAllMessagesOfChat(userId, receiverId).map { message ->
            message.toMessageDto()
        }
    }

    suspend fun uploadImage(name: String, byteArray: ByteArray): String {
        return imageDao.uploadImage(name, byteArray)
    }

    suspend fun sendUserTyping(
        senderId: String,
        receiverId: String
    ) {
        val json = Json.encodeToString(
            value = SocketResponse(
                type = SocketResponseType.USER_TYPING,
                senderId = senderId
            )
        )
        sockets[receiverId]?.send(json)
    }

    suspend fun sendUserStopTyping(
        senderId: String,
        receiverId: String
    ) {
        val json = Json.encodeToString(
            value = SocketResponse(
                type = SocketResponseType.USER_STOP_TYPING,
                senderId = senderId,
            )
        )
        sockets[receiverId]?.send(json)
    }

    suspend fun createMessage(
        senderId: String,
        receiverId: String,
        message: String,
        imageUrl: String?
    ) {
        val timeNow = System.currentTimeMillis()
        val msg = Message(
            senderId = senderId,
            receiverId = receiverId,
            message = message,
            imageUrl = imageUrl,
            createdAtTimestamp = timeNow,
            updatedAtTimestamp = timeNow,
        )
        val json = Json.encodeToString(
            value = SocketResponse(
                type = SocketResponseType.CREATE_MESSAGE,
                message = msg.toMessageDto()
            )
        )
        sockets[senderId]?.send(json)
        sockets[receiverId]?.send(json)
        messageDao.insertMessage(msg)
        sockets[receiverId] ?: return
        val sender = userDao.getUserById(senderId) ?: return
        val receiver = userDao.getUserById(receiverId) ?: return
        receiver.fcmToken ?: return
        notificationDao.sendNotification(
            deviceToken = receiver.fcmToken,
            title = "${sender.username} sent you a message",
            body = message,
            data = hashMapOf(
                "senderId" to senderId,
                "messageId" to msg.id.toString(),
                "message" to message,
            )
        )
    }

    suspend fun updateMessage(
        senderId: String,
        receiverId: String,
        messageId: String,
        message: String?
    ) {
        val msg = messageDao.getMessageById(messageId)
        msg ?: throw MessageNotFoundException(messageId)
        val updatedMessage = msg.copy(
            message = message ?: msg.message,
            updatedAtTimestamp = System.currentTimeMillis()
        )
        val json = Json.encodeToString(
            value = SocketResponse(
                type = SocketResponseType.UPDATE_MESSAGE,
                message = updatedMessage.toMessageDto()
            )
        )
        sockets[senderId]?.send(json)
        sockets[receiverId]?.send(json)
        messageDao.updateMessage(updatedMessage)
    }

    suspend fun disconnect(userId: String) {
        sockets[userId]?.close()
        if (sockets.containsKey(userId)) {
            sockets.remove(userId)
        }
    }

    private data class SocketKey(
        val id: String,
        val expiresAt: Long
    )
}