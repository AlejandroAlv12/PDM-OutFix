package com.pdm0126.outfix.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LentItemDto(
    val id: String,
    val garmentId: String,
    val garmentImageUrl: String? = null,
    val garmentName: String,
    val borrowerName: String,
    val lentDate: String,
    val reclaimDate: String,
    val reclaimDateMillis: Long,
    val isReturned: Boolean = false
)

@Serializable
data class CreateLentItemRequest(
    val garmentId: String,
    val garmentImageUrl: String? = null,
    val garmentName: String,
    val borrowerName: String,
    val lentDate: String,
    val reclaimDate: String,
    val reclaimDateMillis: Long
)
