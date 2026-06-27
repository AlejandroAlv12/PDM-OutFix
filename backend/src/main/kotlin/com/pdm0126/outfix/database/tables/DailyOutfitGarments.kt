package com.pdm0126.outfix.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

// Tabla pivote: qué prenda va en qué slot del outfit
object DailyOutfitGarments : UUIDTable("daily_outfit_garments") {
    val dailyOutfitId = reference("daily_outfit_id", DailyOutfits, onDelete = ReferenceOption.CASCADE)
    val garmentId = reference("garment_id", Garments, onDelete = ReferenceOption.CASCADE)
    val slotType = varchar("slot_type", 20) // TOP, BOTTOM, FOOTWEAR, ACCESSORY, OUTERWEAR
}
