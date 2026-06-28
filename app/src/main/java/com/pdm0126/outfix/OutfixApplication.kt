package com.pdm0126.outfix

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.pdm0126.outfix.data.local.AppDatabase
import com.pdm0126.outfix.data.repository.GarmentRepository
import com.pdm0126.outfix.data.repository.LentRepository
import com.pdm0126.outfix.data.repository.PlannerRepository

class OutfixApplication : Application() {
    
    companion object {
        lateinit var instance: OutfixApplication
            private set

        const val LENT_NOTIFICATION_CHANNEL_ID = "lent_reclaim_channel"
        const val LENT_NOTIFICATION_CHANNEL_NAME = "Prendas por reclamar"
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
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
    
    val database by lazy { AppDatabase.getDatabase(this) }
    
    val garmentRepository by lazy { GarmentRepository(database.garmentDao()) }
    val plannerRepository by lazy { PlannerRepository(database.plannerDayDao(), database.garmentDao()) }
    val lentRepository by lazy { LentRepository(this) }
}
