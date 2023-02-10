package com.nasportfolio.routes.users

import com.nasportfolio.routes.users.requests.LoginRequest
import com.nasportfolio.routes.users.requests.SignUpRequest
import com.nasportfolio.routes.users.requests.UpdateAccountRequest
import com.nasportfolio.routes.users.responses.TokenResponse
import com.nasportfolio.routes.users.responses.toUserDto
import com.nasportfolio.data.user.User
import com.nasportfolio.data.user.UserDao
import com.nasportfolio.security.TokenService
import com.nasportfolio.security.getUserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val userDao by inject<UserDao>()
    val tokenService by inject<TokenService>()
    getAllUsersRoute(userDao)
    searchForUsers(userDao)
    updateAccount(userDao)
    register(userDao, tokenService)
    login(userDao, tokenService)
    deleteAccount(userDao)
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
        val user = userDao.getUserByUsername(username) ?: return@get call.respond(HttpStatusCode.NotFound)
        call.respond(user.toUserDto())
    }
}

private fun Route.searchForUsers(userDao: UserDao) {
    get(path = "/api/users/search") {
        val query = call.request.queryParameters["q"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val users = userDao.searchUsersByUsername(query).map {
            it.toUserDto()
        }
        call.respond(users)
    }
}

private fun Route.updateAccount(userDao: UserDao) {
    authenticate {
        put(path = "/api/users") {
            val userId = call.principal<JWTPrincipal>()!!.getUserId()
            val user = userDao.getUserById(userId) ?: return@put call.respond(HttpStatusCode.NotFound)
            val body = call.receive<UpdateAccountRequest>()
            val updatedUser = user.copy(
                username = body.username ?: user.username,
                email = body.email ?: user.email
            )
            userDao.updateUser(updatedUser)
            call.respond(updatedUser.toUserDto())
        }
    }
}

private fun Route.register(userDao: UserDao, tokenService: TokenService) {
    post("/api/users/register") {
        val body = call.receive<SignUpRequest>()
        val user = User(
            username = body.username,
            email = body.email,
            password = body.password,
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

private fun Route.login(userDao: UserDao, tokenService: TokenService) {
    post("/api/users/login") {
        val body = call.receive<LoginRequest>()
        val user = userDao.getUserByEmail(body.email)
        if (user?.password != body.password) return@post call.respond(HttpStatusCode.Unauthorized)
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
            val userId = call.principal<JWTPrincipal>()!!.getUserId()
            val user = userDao.getUserById(userId) ?: return@delete call.respond(HttpStatusCode.NotFound)
            userDao.deleteUser(user)
            call.respond(HttpStatusCode.OK)
        }
    }
}