package com.pdm0126.outfix.screens.menu
import android.content.Context
import com.pdm0126.outfix.data.local.LentItem
import com.pdm0126.outfix.ui.ReclaimNotificationWorker

fun scheduleReclaimNotification(context: Context, item: LentItem) {
    val delayMs = item.reclaimDateMillis - System.currentTimeMillis()
    if (delayMs <= 0) return

    val workRequest = androidx.work.OneTimeWorkRequestBuilder<ReclaimNotificationWorker>()
        .setInitialDelay(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
        .setInputData(
            androidx.work.workDataOf(
                "item_id" to item.id,
                "borrower_name" to item.borrowerName,
                "garment_name" to item.garmentName
            )
        )
        .addTag("reclaim_${item.id}")
        .build()

    androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
}

fun cancelReclaimNotification(context: Context, itemId: String) {
    androidx.work.WorkManager.getInstance(context).cancelAllWorkByTag("reclaim_$itemId")
}
