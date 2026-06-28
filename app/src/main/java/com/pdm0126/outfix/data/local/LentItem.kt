package com.pdm0126.outfix.data.local

import kotlinx.serialization.Serializable

@Serializable
data class LentItem(
    val id: String,
    val garmentId: String,
    val garmentImageUrl: String? = null,
    val garmentName: String,
    val borrowerName: String,
    val lentDate: String,          // e.g. "28/06"
    val reclaimDate: String,       // e.g. "15/10"
    val reclaimDateMillis: Long,   // for scheduling notifications
    val isReturned: Boolean = false
)
