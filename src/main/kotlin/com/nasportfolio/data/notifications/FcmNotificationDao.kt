package com.nasportfolio.data.notifications

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification

class FcmNotificationDao(
    private val fcmMessaging: FirebaseMessaging
) : NotificationDao {
    override fun sendNotification(
        deviceToken: String,
        title: String,
        body: String,
        data: HashMap<String, String>
    ) {
        val notification = Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build()
        val message = Message.builder()
            .setNotification(notification)
            .apply { data.forEach { (k, v) -> putData(k, v) } }
            .setToken(deviceToken)
            .build()
        fcmMessaging.send(message)
    }
}