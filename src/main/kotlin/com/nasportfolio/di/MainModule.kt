package com.nasportfolio.di

import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.cloud.StorageClient
import com.google.firebase.messaging.FirebaseMessaging
import com.nasportfolio.data.images.FirebaseImageDao
import com.nasportfolio.data.images.ImageDao
import com.nasportfolio.data.message.MessageDao
import com.nasportfolio.data.message.MongoMessageDao
import com.nasportfolio.data.notifications.FcmNotificationDao
import com.nasportfolio.data.notifications.NotificationDao
import com.nasportfolio.data.user.MongoUserDao
import com.nasportfolio.data.user.UserDao
import com.nasportfolio.routes.messages.MessageService
import com.nasportfolio.security.BcryptHashingService
import com.nasportfolio.security.HashingService
import com.nasportfolio.security.JwtTokenService
import com.nasportfolio.security.TokenService
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
        val password = System.getenv("MONGO_PASSWORD")
        val database = System.getenv("DATABASE")
        val connectionString = "mongodb+srv://" +
                "Coeeter:$password@cluster0.0y8th62.mongodb.net/" +
                "?retryWrites=true&w=majority"
        KMongo.createClient(connectionString)
            .coroutine
            .getDatabase(database)
    }

    single {
        MongoUserDao(get())
    } bind UserDao::class

    single {
        MongoMessageDao(get())
    } bind MessageDao::class

    single {
        FcmNotificationDao(FirebaseMessaging.getInstance())
    } bind NotificationDao::class

    single {
        FirebaseImageDao(StorageClient.getInstance().bucket())
    } bind ImageDao::class

    single {
        MessageService(get(), get(), get(), get())
    }
}