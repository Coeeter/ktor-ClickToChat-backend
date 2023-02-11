package com.nasportfolio.routes.users

import com.nasportfolio.data.images.ImageDao
import com.nasportfolio.data.user.User
import com.nasportfolio.data.user.UserDao
import com.nasportfolio.data.user.user
import com.nasportfolio.routes.users.requests.LoginRequest
import com.nasportfolio.routes.users.requests.SignUpRequest
import com.nasportfolio.routes.users.requests.UpdateAccountRequest
import com.nasportfolio.routes.users.responses.TokenResponse
import com.nasportfolio.routes.users.responses.toUserDto
import com.nasportfolio.security.HashingService
import com.nasportfolio.security.TokenService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.userRoutes() {
    val userDao by inject<UserDao>()
    val tokenService by inject<TokenService>()
    val hashingService by inject<HashingService>()
    val imageDao by inject<ImageDao>()

    getAllUsersRoute(userDao)
    searchForUsers(userDao)
    updateAccount(userDao)
    register(userDao, tokenService, hashingService)
    login(userDao, tokenService, hashingService)
    deleteAccount(userDao)
    deleteToken(userDao)
    validateToken()
    uploadImage(userDao, imageDao)
    deleteImage(userDao, imageDao)
    getUserByUsername(userDao)
}

private fun Route.getAllUsersRoute(userDao: UserDao) {
    get(path = "/api/users") {
        call.respond(userDao.getAllUsers().map { it.toUserDto() })
    }
}

private fun Route.getUserByUsername(userDao: UserDao) {
    get(path = "/api/users/{username}") {
        val username = call.parameters["username"]!!
        val user = userDao.getUserByUsername(username)
        user ?: return@get call.respond(HttpStatusCode.NotFound)
        call.respond(user.toUserDto())
    }
}

private fun Route.searchForUsers(userDao: UserDao) {
    get(path = "/api/users/search") {
        val query = call.request.queryParameters["q"]
        query ?: return@get call.respond(HttpStatusCode.BadRequest)
        val users = userDao.searchUsersByUsername(query).map {
            it.toUserDto()
        }
        call.respond(users)
    }
}

private fun Route.updateAccount(userDao: UserDao) {
    authenticate {
        put(path = "/api/users") {
            val user = call.user!!
            val body = call.receive<UpdateAccountRequest>()
            val updatedUser = user.copy(
                username = body.username ?: user.username,
                email = body.email ?: user.email,
                fcmToken = body.fcmToken ?: user.fcmToken
            )
            userDao.updateUser(updatedUser)
            call.respond(updatedUser.toUserDto())
        }
    }
}

private fun Route.register(
    userDao: UserDao,
    tokenService: TokenService,
    hashingService: HashingService
) {
    post("/api/users/register") {
        val body = call.receive<SignUpRequest>()
        val hashedPassword = hashingService.hash(body.password)
        val user = User(
            username = body.username,
            email = body.email,
            password = hashedPassword,
            fcmToken = body.fcmToken,
            createdAtTimeStamp = System.currentTimeMillis()
        )
        userDao.insertUser(user)
        val token = tokenService.generateToken(
            claims = listOf(
                TokenService.TokenClaim(
                    key = "userId",
                    value = user.id.toString()
                )
            )
        )
        call.respond(TokenResponse(token))
    }
}

private fun Route.login(
    userDao: UserDao,
    tokenService: TokenService,
    hashingService: HashingService
) {
    post("/api/users/login") {
        val body = call.receive<LoginRequest>()
        val user = userDao.getUserByEmail(body.email)
        user ?: return@post call.respond(HttpStatusCode.NotFound)
        if (hashingService.verify(body.password, user.password)) {
            return@post call.respond(HttpStatusCode.Unauthorized)
        }
        val token = tokenService.generateToken(
            claims = listOf(
                TokenService.TokenClaim(
                    key = "userId",
                    value = user.id.toString()
                )
            )
        )
        call.respond(TokenResponse(token))
    }
}

private fun Route.deleteAccount(userDao: UserDao) {
    authenticate {
        delete("/api/users") {
            val user = call.user!!
            userDao.deleteUser(user)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.deleteToken(userDao: UserDao) {
    authenticate {
        delete("/api/users/token") {
            val user = call.user!!
            val updatedUser = user.copy(fcmToken = null)
            userDao.updateUser(updatedUser)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.validateToken() {
    authenticate {
        get("/api/users/validate-token") {
            val user = call.user!!
            call.respond(user.toUserDto())
        }
    }
}

private fun Route.uploadImage(userDao: UserDao, imageDao: ImageDao) {
    authenticate {
        post("/api/users/images") {
            val user = call.user!!
            val request = call.receiveMultipart()
            request.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val byteArray = part.streamProvider().readBytes()
                        val fileExtension = part.originalFileName?.takeLastWhile { it != '.' }
                        val fileName = UUID.randomUUID().toString() + '.' + fileExtension
                        val imageUrl = imageDao.uploadImage(fileName, byteArray)
                        val updatedUser = user.copy(imageUrl = imageUrl)
                        userDao.updateUser(updatedUser)
                        call.respond(updatedUser.toUserDto())
                    }

                    else -> Unit
                }
            }
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}

private fun Route.deleteImage(userDao: UserDao, imageDao: ImageDao) {
    authenticate {
        delete("/api/users/images") {
            val user = call.user!!
            user.imageUrl?.let { key -> imageDao.deleteImage(key) }
            val updatedUser = user.copy(imageUrl = null)
            userDao.updateUser(updatedUser)
            call.respond(updatedUser.toUserDto())
        }
    }
}