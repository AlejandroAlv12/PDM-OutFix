package com.pdm0126.outfix.ui

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pdm0126.outfix.OutfixApplication
import com.pdm0126.outfix.R

class ReclaimNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val borrowerName = inputData.getString("borrower_name") ?: "alguien"
        val garmentName = inputData.getString("garment_name") ?: "una prenda"

        val notification = NotificationCompat.Builder(context, OutfixApplication.LENT_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("¡Hora de reclamar tu prenda!")
            .setContentText("Recuerda pedirle a $borrowerName que te devuelva: $garmentName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }
}
