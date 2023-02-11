package com.nasportfolio

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.InputStream

object FirebaseAdmin {
    private val serviceAccount: InputStream? = this::class.java
        .classLoader
        .getResourceAsStream("clicktochat-private-key.json")

    private val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setStorageBucket(System.getenv("STORAGE_BUCKET_NAME"))
        .build()

    fun init(): FirebaseApp = FirebaseApp.initializeApp(options)
}