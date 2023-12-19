package com.android.sitbak.home.notifications

data class NotificationsModel(
    val `data`: List<NotificationsData>,
    val message: String,
    val status: Boolean
)

data class NotificationsData(
    val created_at: String,
    val id: Int,
    val is_read: Int,
    val notification: String,
    val notification_type: String,
    val time: String,
    val title: String
)