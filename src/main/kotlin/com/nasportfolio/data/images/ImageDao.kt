package com.nasportfolio.data.images

interface ImageDao {
    suspend fun uploadImage(name: String, data: ByteArray): String
    suspend fun deleteImage(url: String): Boolean
}