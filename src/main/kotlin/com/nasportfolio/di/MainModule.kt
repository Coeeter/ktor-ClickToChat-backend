package com.nasportfolio.di

import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.cloud.StorageClient
import com.google.firebase.messaging.FirebaseMessaging
import com.nasportfolio.EnvConfig
import com.nasportfolio.data.images.FirebaseImageDao
import com.nasportfolio.data.images.ImageDao
import com.nasportfolio.data.message.MessageDao
import com.nasportfolio.data.message.MongoMessageDao
import com.nasportfolio.data.user.MongoUserDao
import com.nasportfolio.data.user.UserDao
import com.nasportfolio.routes.messages.MessageService
import com.nasportfolio.services.notifications.FcmNotificationService
import com.nasportfolio.services.notifications.NotificationService
import com.nasportfolio.services.security.BcryptHashingService
import com.nasportfolio.services.security.HashingService
import com.nasportfolio.services.security.JwtTokenService
import com.nasportfolio.services.security.TokenService
import org.koin.dsl.bind
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mainModule = module {
    factory {
        JwtTokenService()
    } bind TokenService::class

    factory {
        BcryptHashingService(
            hasher = BCrypt.withDefaults(),
            verifier = BCrypt.verifyer()
        )
    } bind HashingService::class

    single {
        val connectionString = "mongodb+srv://Coeeter:${EnvConfig.mongoDbPassword}" +
                "@cluster0.0y8th62.mongodb.net/?retryWrites=true&w=majority"
        KMongo.createClient(connectionString)
            .coroutine
            .getDatabase(EnvConfig.databaseName)
    }

    single {
        MongoUserDao(get())
    } bind UserDao::class

    single {
        MongoMessageDao(get())
    } bind MessageDao::class

    single {
        FcmNotificationService(FirebaseMessaging.getInstance())
    } bind NotificationService::class

    single {
        FirebaseImageDao(StorageClient.getInstance().bucket())
    } bind ImageDao::class

    single {
        MessageService(get(), get(), get(), get())
    }
}