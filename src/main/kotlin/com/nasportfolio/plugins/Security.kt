package com.nasportfolio.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.nasportfolio.EnvConfig
import com.nasportfolio.data.user.UserDao
import com.nasportfolio.services.security.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val userDao by inject<UserDao>()

    authentication {
        jwt {
            realm = "ClickToChat"
            val secret = EnvConfig.jwtSecret
            val verifier = JWT.require(Algorithm.HMAC256(secret))
                .withAudience(TokenService.DEFAULT_AUDIENCE)
                .withIssuer(TokenService.DEFAULT_ISSUER)
                .build()
            verifier(verifier)
            validate { credential ->
                credential.payload.getClaim("userId")?.asString()?.let {
                    userDao.getUserById(id = it)
                }
            }
            challenge { _, _ ->
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = mapOf("message" to "Invalid token provided")
                )
            }
        }
    }
}
