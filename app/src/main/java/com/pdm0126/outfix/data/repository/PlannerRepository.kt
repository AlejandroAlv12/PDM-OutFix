package com.pdm0126.outfix.data.repository

import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.local.GarmentDao
import com.pdm0126.outfix.data.local.PlannerDayDao
import com.pdm0126.outfix.data.local.PlannerDayEntity
import com.pdm0126.outfix.data.model.DayInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import androidx.compose.ui.graphics.Color
import java.util.Calendar

class PlannerRepository(
    private val plannerDayDao: PlannerDayDao,
    private val garmentDao: GarmentDao
) {
    private val defaultDays = listOf(
        DayInfo("LUN", Calendar.MONDAY, Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent),
        DayInfo("MAR", Calendar.TUESDAY, Color.Transparent, Color.Transparent, Color.Transparent, null),
        DayInfo("MIE", Calendar.WEDNESDAY, Color.Transparent, Color.Transparent, Color.Transparent, null),
        DayInfo("JUE", Calendar.THURSDAY, Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent),
        DayInfo("VIE", Calendar.FRIDAY, Color.Transparent, Color.Transparent, Color.Transparent, null),
        DayInfo("SAB", Calendar.SATURDAY, Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent),
        DayInfo("DOM", Calendar.SUNDAY, Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent)
    )

    val plannerDaysFlow: Flow<List<DayInfo>> = combine(
        plannerDayDao.getAllFlow(),
        garmentDao.getAllGarmentsFlow()
    ) { dayEntities, garmentEntities ->
        val garmentsMap = garmentEntities.associate { it.id to it.toDto() }
        val daysMap = dayEntities.associateBy { it.dayKey }

        defaultDays.map { defaultDay ->
            val entity = daysMap[defaultDay.day]
            if (entity != null) {
                defaultDay.copy(
                    topGarment = entity.topGarmentId?.let { garmentsMap[it] },
                    bottomGarment = entity.bottomGarmentId?.let { garmentsMap[it] },
                    shoesGarment = entity.shoesGarmentId?.let { garmentsMap[it] },
                    hatGarment = entity.hatGarmentId?.let { garmentsMap[it] },
                    accessories = entity.accessoryIds.split(",").filter { it.isNotBlank() }.mapNotNull { garmentsMap[it] }
                )
            } else {
                defaultDay
            }
        }
    }

    suspend fun saveDayOutfit(
        dayKey: String,
        top: GarmentResponse?,
        bottom: GarmentResponse?,
        shoes: GarmentResponse?,
        head: GarmentResponse?,
        accessories: List<GarmentResponse>
    ) {
        val entity = PlannerDayEntity(
            dayKey = dayKey,
            topGarmentId = top?.id,
            bottomGarmentId = bottom?.id,
            shoesGarmentId = shoes?.id,
            hatGarmentId = head?.id,
            accessoryIds = accessories.joinToString(",") { it.id }
        )
        withContext(Dispatchers.IO) {
            plannerDayDao.upsertDay(entity)
        }
    }

    suspend fun restorePlannerDays() {
    }

    suspend fun clearDayOutfit(dayKey: String) {
        val entity = PlannerDayEntity(
            dayKey = dayKey,
            topGarmentId = null,
            bottomGarmentId = null,
            shoesGarmentId = null,
            hatGarmentId = null,
            accessoryIds = ""
        )
        withContext(Dispatchers.IO) {
            plannerDayDao.upsertDay(entity)
        }
    }
}
