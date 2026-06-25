package com.pdm0126.outfix.models

import kotlinx.serialization.Serializable

// --- Request DTOs ---

@Serializable
data class CreateWeeklyPlanRequest(
    val weekStartDate: String,   // "2026-06-24"
    val label: String? = null
)

@Serializable
data class CreateDailyOutfitRequest(
    val weeklyPlanId: String,
    val dayOfWeek: String,       // MONDAY, TUESDAY, etc.
    val occasion: String? = null
)

@Serializable
data class AssignGarmentSlotRequest(
    val garmentId: String,
    val slotType: String         // TOP, BOTTOM, FOOTWEAR, ACCESSORY, OUTERWEAR
)

// --- Response DTOs ---

@Serializable
data class WeeklyPlanResponse(
    val id: String,
    val userId: String,
    val weekStartDate: String,
    val label: String?,
    val dailyOutfits: List<DailyOutfitResponse> = emptyList()
)

@Serializable
data class DailyOutfitResponse(
    val id: String,
    val weeklyPlanId: String,
    val dayOfWeek: String,
    val occasion: String?,
    val garments: List<OutfitGarmentResponse> = emptyList()
)

@Serializable
data class OutfitGarmentResponse(
    val id: String,
    val garmentId: String,
    val slotType: String,
    val garment: GarmentResponse? = null
)
