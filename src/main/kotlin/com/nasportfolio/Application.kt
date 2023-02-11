package com.nasportfolio

import com.nasportfolio.di.mainModule
import com.nasportfolio.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin


fun main(args: Array<String>) {
    FirebaseAdmin.init()
    EngineMain.main(args)
}

fun Application.module() {
    install(Koin) { modules(mainModule) }
    configureSerialization()
    configureSockets()
    configureMonitoring()
    configureSecurity()
    configureStatusPages()
    configureRouting()
}
