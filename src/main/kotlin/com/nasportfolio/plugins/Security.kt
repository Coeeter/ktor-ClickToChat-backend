package com.nasportfolio.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.nasportfolio.security.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    authentication {
        jwt {
            realm = "ClickToChat"
            val secret = System.getenv("JWT_SECRET")
            val verifier = JWT.require(Algorithm.HMAC256(secret))
                .withAudience(TokenService.DEFAULT_AUDIENCE)
                .withIssuer(TokenService.DEFAULT_ISSUER)
                .build()
            verifier(verifier)
            validate { credential ->
                val isValidToken = credential.payload
                    .audience
                    .contains(TokenService.DEFAULT_AUDIENCE)
                if (!isValidToken) return@validate null
                JWTPrincipal(credential.payload)
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
