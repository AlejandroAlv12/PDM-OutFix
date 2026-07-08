package com.pdm0126.outfix

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OutfixApplication : Application() {

    companion object {

        const val LENT_NOTIFICATION_CHANNEL_ID = "lent_reclaim_channel"
        const val LENT_NOTIFICATION_CHANNEL_NAME = "Prendas por reclamar"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        com.pdm0126.outfix.utils.NotificationHelper.scheduleSunsetNotification(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LENT_NOTIFICATION_CHANNEL_ID,
                LENT_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para recordar reclamar prendas prestadas"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
