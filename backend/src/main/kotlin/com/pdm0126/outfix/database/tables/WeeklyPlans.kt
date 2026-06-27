package com.pdm0126.outfix.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

// Plan semanal (ej: "Semana del 24 de junio")
object WeeklyPlans : UUIDTable("weekly_plans") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val weekStartDate = varchar("week_start_date", 10) // "2026-06-24"
    val label = varchar("label", 100).nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
}
