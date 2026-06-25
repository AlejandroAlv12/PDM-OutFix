package com.pdm0126.outfix.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

// Tabla principal de prendas
object Garments : UUIDTable("garments") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 100)
    val category = varchar("category", 30)       // TOP, BOTTOM, FOOTWEAR, ACCESSORY, OUTERWEAR
    val colorHex = varchar("color_hex", 10).nullable()   // Ej: "#FF5733"
    val colorName = varchar("color_name", 50).nullable() // Ej: "Rojo"
    val status = varchar("status", 20).default("AVAILABLE") // AVAILABLE, DIRTY, LOANED
    val imageUrl = varchar("image_url", 500).nullable()
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}
