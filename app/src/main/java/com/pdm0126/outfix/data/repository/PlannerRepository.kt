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
        DayInfo("LUN", Calendar.MONDAY, Color.White, Color(0xFF90D5E1), Color(0xFF277636), Color(0xFF90D5E1)),
        DayInfo("MAR", Calendar.TUESDAY, Color(0xFF67B0E8), Color.Black, Color.Black, null),
        DayInfo("MIE", Calendar.WEDNESDAY, Color(0xFFFF85E2), Color.White, Color.Black, null),
        DayInfo("JUE", Calendar.THURSDAY, Color(0xFFFF1010), Color.White, Color.White, Color(0xFFFF1010)),
        DayInfo("VIE", Calendar.FRIDAY, Color(0xFFE4A9B0), Color(0xFF94C4E1), Color(0xFFE4A9B0), null),
        DayInfo("SAB", Calendar.SATURDAY, Color(0xFF4C8CA8), Color(0xFF033348), Color.Black, Color(0xFF4C8CA8)),
        DayInfo("DOM", Calendar.SUNDAY, Color(0xFF8F8F8F), Color.Black, Color.Black, Color.Black)
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
