package com.nasportfolio.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<Throwable> { call, cause ->
            call.respond(
                message = mapOf("error" to "500: $cause"),
                status = HttpStatusCode.InternalServerError
            )
        }
    }
}