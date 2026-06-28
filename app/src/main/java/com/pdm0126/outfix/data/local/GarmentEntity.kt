package com.pdm0126.outfix.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pdm0126.outfix.data.api.dto.GarmentResponse

@Entity(tableName = "garments")
data class GarmentEntity(
    @PrimaryKey val id: String,
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
    val updatedAt: String,
    val isPendingSync: Boolean = false
) {
    fun toDto(): GarmentResponse {
        return GarmentResponse(
            id = id,
            userId = userId,
            name = name,
            category = category,
            colorHex = colorHex,
            colorName = colorName,
            style = style,
            brand = brand,
            size = size,
            status = status,
            imageUrl = imageUrl,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

fun GarmentResponse.toEntity(isPendingSync: Boolean = false): GarmentEntity {
    return GarmentEntity(
        id = id,
        userId = userId,
        name = name,
        category = category,
        colorHex = colorHex,
        colorName = colorName,
        style = style,
        brand = brand,
        size = size,
        status = status,
        imageUrl = imageUrl,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPendingSync = isPendingSync
    )
}
