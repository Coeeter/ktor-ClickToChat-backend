package com.nasportfolio

object EnvConfig {
    val storageBucketName: String = System.getenv("STORAGE_BUCKET_NAME")
    val mongoDbPassword: String = System.getenv("MONGO_PASSWORD")
    val databaseName: String = System.getenv("DATABASE")
    val jwtSecret: String = System.getenv("JWT_SECRET")
}