package com.nasportfolio.services.notifications

interface NotificationService {
    fun sendNotification(
        deviceToken: String,
        title: String,
        body: String,
        data: HashMap<String, String> = hashMapOf()
    )
}