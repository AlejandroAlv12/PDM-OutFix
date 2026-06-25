package com.pdm0126.outfix.models

import kotlinx.serialization.Serializable

// --- Request DTOs ---

@Serializable
data class CreateGarmentRequest(
    val name: String,
    val category: String,     // TOP, BOTTOM, FOOTWEAR, ACCESSORY, OUTERWEAR
    val colorHex: String? = null,
    val colorName: String? = null,
    val status: String = "AVAILABLE",
    val imageUrl: String? = null,
    val notes: String? = null
)

@Serializable
data class UpdateGarmentRequest(
    val name: String? = null,
    val category: String? = null,
    val colorHex: String? = null,
    val colorName: String? = null,
    val status: String? = null,
    val imageUrl: String? = null,
    val notes: String? = null
)

// --- Response DTO ---

@Serializable
data class GarmentResponse(
    val id: String,
    val userId: String,
    val name: String,
    val category: String,
    val colorHex: String?,
    val colorName: String?,
    val status: String,
    val imageUrl: String?,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)
