package com.nasportfolio.routes.messages

import com.nasportfolio.data.user.user
import com.nasportfolio.routes.messages.exceptions.InvalidKeyException
import com.nasportfolio.routes.messages.exceptions.MessageNotFoundException
import com.nasportfolio.routes.messages.exceptions.UserAlreadyConnectedException
import com.nasportfolio.routes.messages.requests.SocketRequest
import com.nasportfolio.routes.messages.requests.SocketRequestType
import com.nasportfolio.routes.messages.responses.UrlDto
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.util.*

fun Route.messageRoutes() {
    val messageService by inject<MessageService>()
    getAllMessagesOfChat(messageService)
    getAllMessagesToUser(messageService)
    uploadImage(messageService)
    authenticateSocket(messageService)
    chatSocket(messageService)
}

private fun Route.getAllMessagesOfChat(messageService: MessageService) {
    authenticate {
        get("/api/messages/{receiverId}") {
            val userId = call.user!!.id.toString()
            val receiverId = call.parameters["receiverId"]!!
            try {
                val participants = listOf(userId, receiverId)
                call.respond(messageService.getMessagesOfChat(participants))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private fun Route.getAllMessagesToUser(messageService: MessageService) {
    authenticate {
        get("/api/messages") {
            val userId = call.user!!.id.toString()
            call.respond(messageService.getAllMessagesOfUser(userId = userId))
        }
    }
}

private fun Route.uploadImage(messageService: MessageService) {
    authenticate {
        post("/api/messages/images") {
            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val bytes = part.streamProvider().readBytes()
                        val fileExtension = part.originalFileName?.takeLastWhile { it != '.' }
                        val fileName = UUID.randomUUID().toString() + '.' + fileExtension
                        val url = messageService.uploadImage(fileName, bytes)
                        call.respond(UrlDto(url))
                    }

                    else -> Unit
                }
            }
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}

private fun Route.authenticateSocket(messageService: MessageService) {
    authenticate {
        get("/api/messages/authenticate") {
            val user = call.user!!
            val key = messageService.generateKey(user.id.toString())
            val url = "/api/messages/chat-socket?u=${user.id}&k=$key"
            call.respond(UrlDto(url))
        }
    }
}

private fun Route.chatSocket(messageService: MessageService) {
    webSocket("/api/messages/chat-socket") {
        val userId = call.request.queryParameters["u"]!!
        val key = call.request.queryParameters["k"]!!
        try {
            messageService.onJoin(
                userId = userId,
                key = key,
                socket = this
            )
            incoming.consumeEach { frame ->
                if (frame !is Frame.Text) return@consumeEach
                val request = Json.decodeFromString<SocketRequest>(frame.readText())
                when (request.type) {
                    SocketRequestType.CREATE -> messageService.createMessage(
                        senderId = userId,
                        receiverId = request.createMessageRequest!!.receiverId,
                        message = request.createMessageRequest.message,
                        imageUrl = request.createMessageRequest.imageUrl
                    )

                    SocketRequestType.UPDATE -> messageService.updateMessage(
                        senderId = userId,
                        receiverId = request.updateMessageRequest!!.receiverId,
                        messageId = request.updateMessageRequest.messageId!!,
                        message = request.updateMessageRequest.message,
                    )

                    SocketRequestType.TYPING -> messageService.sendUserTyping(
                        senderId = userId,
                        receiverId = request.receiverId!!
                    )

                    SocketRequestType.STOP_TYPING -> messageService.sendUserStopTyping(
                        senderId = userId,
                        receiverId = request.receiverId!!
                    )

                    SocketRequestType.MESSAGE_SEEN -> messageService.setMessagesSeen(
                        receiverId = request.messageSeenRequest!!.receiverId,
                        messagesSeen = request.messageSeenRequest.messages
                    )
                }
            }
        } catch (e: MessageNotFoundException) {
            call.respond(HttpStatusCode.NotFound, e.message.toString())
        } catch (e: UserAlreadyConnectedException) {
            call.respond(HttpStatusCode.BadRequest, e.message.toString())
        } catch (e: InvalidKeyException) {
            call.respond(HttpStatusCode.Forbidden, e.message.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            messageService.disconnect(userId)
        }
    }
}