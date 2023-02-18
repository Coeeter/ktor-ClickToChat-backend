package com.nasportfolio.plugins

import com.nasportfolio.routes.messages.messageRoutes
import com.nasportfolio.routes.users.userRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route(path = "/api") {
            userRoutes()
            messageRoutes()
        }
    }
}
