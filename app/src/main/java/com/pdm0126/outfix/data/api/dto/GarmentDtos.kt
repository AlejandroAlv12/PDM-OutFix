package com.pdm0126.outfix.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateGarmentRequest(
    val name: String,
    val category: String,
    val colorHex: String? = null,
    val colorName: String? = null,
    val style: String? = null,
    val brand: String? = null,
    val size: String? = null,
    val status: String = "AVAILABLE",
    val imageUrl: String? = null,
    val notes: String? = null
)

@Serializable
data class GarmentResponse(
    val id: String,
    val userId: String,
    val name: String,
    val category: String,
    val colorHex: String?,
    val colorName: String?,
    val style: String?,
    val brand: String?,
    val size: String?,
    val status: String,
    val imageUrl: String?,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)
