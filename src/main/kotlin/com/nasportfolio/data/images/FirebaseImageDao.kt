package com.nasportfolio.data.images

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Bucket

class FirebaseImageDao(
    private val bucket: Bucket
) : ImageDao {
    override suspend fun uploadImage(name: String, data: ByteArray): String {
        val blobId = BlobId.of(bucket.name, name)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("media")
            .build()
        val blob = bucket.storage.create(blobInfo, data)
        return buildImageUrl(blob.name)
    }

    override suspend fun deleteImage(url: String): Boolean {
        return bucket[extractKeyFromUrl(url)].delete()
    }

    private fun buildImageUrl(fileName: String): String {
        return "https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/${fileName}?alt=media"
    }

    private fun extractKeyFromUrl(url: String): String {
        val startIndex = url.lastIndexOf("/") + 1
        val endIndex = url.indexOf("?alt=media")
        return url.substring(startIndex, endIndex)
    }
}