package com.nasportfolio.data.notifications

interface NotificationDao {
    fun sendNotification(
        deviceToken: String,
        title: String,
        body: String,
        data: HashMap<String, String> = hashMapOf()
    )
}