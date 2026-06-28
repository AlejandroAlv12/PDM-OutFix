package com.pdm0126.outfix.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "planner_days")
data class PlannerDayEntity(
    @PrimaryKey val dayKey: String,
    val topGarmentId: String? = null,
    val bottomGarmentId: String? = null,
    val shoesGarmentId: String? = null,
    val hatGarmentId: String? = null,
    val accessoryIds: String = ""         
)
