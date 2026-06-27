package com.pdm0126.outfix.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

// Outfit de un día específico dentro de un plan
object DailyOutfits : UUIDTable("daily_outfits") {
    val weeklyPlanId = reference("weekly_plan_id", WeeklyPlans, onDelete = ReferenceOption.CASCADE)
    val dayOfWeek = varchar("day_of_week", 10) // MONDAY, TUESDAY, etc.
    val occasion = varchar("occasion", 100).nullable() // "Universidad", "Gym"
}
