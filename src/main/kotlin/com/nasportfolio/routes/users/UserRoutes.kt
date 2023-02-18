package com.nasportfolio.routes.users

import com.nasportfolio.data.images.ImageDao
import com.nasportfolio.data.user.User
import com.nasportfolio.data.user.UserDao
import com.nasportfolio.data.user.user
import com.nasportfolio.routes.users.requests.LoginRequest
import com.nasportfolio.routes.users.requests.SignUpRequest
import com.nasportfolio.routes.users.requests.UpdateAccountRequest
import com.nasportfolio.routes.users.requests.UpdatePasswordRequest
import com.nasportfolio.routes.users.responses.TokenResponse
import com.nasportfolio.routes.users.responses.toUserDto
import com.nasportfolio.services.security.HashingService
import com.nasportfolio.services.security.TokenService
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

    route("/users") {
        getAllUsersRoute(userDao)
        searchForUsers(userDao)
        updateAccount(userDao)
        updatePassword(userDao, hashingService)
        register(userDao, tokenService, hashingService)
        login(userDao, tokenService, hashingService)
        deleteAccount(userDao)
        deleteToken(userDao)
        validateToken()
        uploadImage(userDao, imageDao)
        deleteImage(userDao, imageDao)
        getUserById(userDao)
    }
}

private fun Route.getAllUsersRoute(userDao: UserDao) {
    get {
        call.respond(userDao.getAllUsers().map { it.toUserDto() })
    }
}

private fun Route.getUserById(userDao: UserDao) {
    get(path = "/{userId}") {
        val userId = call.parameters["userId"]!!
        try {
            val user = userDao.getUserById(userId)
            user ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(user.toUserDto())
        } catch (e: Exception) {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}

private fun Route.searchForUsers(userDao: UserDao) {
    get(path = "/search") {
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
        put {
            val user = call.user!!
            val body = call.receive<UpdateAccountRequest>()
            val updatedUser = user.copy(
                username = body.username ?: user.username,
                email = body.email ?: user.email,
                fcmToken = body.fcmToken ?: user.fcmToken
            )
            body.email?.let { email ->
                userDao.getUserByEmail(email)
            }?.let {
                return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = mapOf("error" to "Email already in use!")
                )
            }
            body.username?.let { username ->
                userDao.getUserByUsername(username)
            }?.let {
                return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = mapOf("error" to "Username already in use!")
                )
            }
            userDao.updateUser(updatedUser)
            call.respond(updatedUser.toUserDto())
        }
    }
}

private fun Route.updatePassword(
    userDao: UserDao,
    hashingService: HashingService
) {
    authenticate {
        put("/password") {
            val user = call.user!!
            val request = call.receive<UpdatePasswordRequest>()
            val isCorrectOldPassword = hashingService.verify(request.oldPassword, user.password)
            if (!isCorrectOldPassword) return@put call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid old password given")
            )
            val isSamePassword = request.oldPassword == request.password
            if (isSamePassword) return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "New password cannot be same as old password!")
            )
            val updatedUser = user.copy(
                password = hashingService.hash(request.password)
            )
            userDao.updateUser(updatedUser)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.register(
    userDao: UserDao,
    tokenService: TokenService,
    hashingService: HashingService
) {
    post("/register") {
        val body = call.receive<SignUpRequest>()
        val hashedPassword = hashingService.hash(body.password)
        val user = User(
            username = body.username,
            email = body.email,
            password = hashedPassword,
            fcmToken = body.fcmToken,
            createdAtTimeStamp = System.currentTimeMillis()
        )
        userDao.getUserByEmail(body.email)?.let {
            return@post call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf("error" to "Email already in use!")
            )
        }
        userDao.getUserByUsername(body.username)?.let {
            return@post call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf("error" to "Username already in use!")
            )
        }
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
    post("/login") {
        val body = call.receive<LoginRequest>()
        val user = userDao.getUserByEmail(body.email)
        user ?: return@post call.respond(HttpStatusCode.NotFound)
        if (!hashingService.verify(body.password, user.password)) {
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
        delete {
            val user = call.user!!
            userDao.deleteUser(user)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.deleteToken(userDao: UserDao) {
    authenticate {
        delete("/token") {
            val user = call.user!!
            val updatedUser = user.copy(fcmToken = null)
            userDao.updateUser(updatedUser)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.validateToken() {
    authenticate {
        get("/validate-token") {
            val user = call.user!!
            call.respond(user.toUserDto())
        }
    }
}

private fun Route.uploadImage(userDao: UserDao, imageDao: ImageDao) {
    authenticate {
        post("/images") {
            val user = call.user!!
            val request = call.receiveMultipart()
            request.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val byteArray = part.streamProvider().readBytes()
                        val fileExtension = part.originalFileName?.takeLastWhile { it != '.' }
                        val fileName = UUID.randomUUID().toString() + '.' + fileExtension
                        val imageUrl = imageDao.uploadImage(fileName, byteArray)
                        user.imageUrl?.let { imageDao.deleteImage(it) }
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
        delete("/images") {
            val user = call.user!!
            user.imageUrl?.let { url -> imageDao.deleteImage(url) }
            val updatedUser = user.copy(imageUrl = null)
            userDao.updateUser(updatedUser)
            call.respond(updatedUser.toUserDto())
        }
    }
}