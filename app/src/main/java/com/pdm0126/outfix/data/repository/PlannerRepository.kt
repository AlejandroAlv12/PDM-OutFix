package com.pdm0126.outfix.data.repository

import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.local.GarmentDao
import com.pdm0126.outfix.data.local.PlannerDayDao
import com.pdm0126.outfix.data.local.PlannerDayEntity
import com.pdm0126.outfix.data.mock.DayInfo
import com.pdm0126.outfix.data.mock.MockDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlannerRepository(
    private val plannerDayDao: PlannerDayDao,
    private val garmentDao: GarmentDao
) {


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

        MockDatabase.plannerDays.find { it.day == dayKey }?.let { dayInfo ->
            dayInfo.topGarment = top
            dayInfo.bottomGarment = bottom
            dayInfo.shoesGarment = shoes
            dayInfo.hatGarment = head
            dayInfo.accessories = accessories.toList()
        }
    }


    suspend fun restorePlannerDays() {
        val entities = withContext(Dispatchers.IO) { plannerDayDao.getAll() }
        if (entities.isEmpty()) return

        val allGarments = withContext(Dispatchers.IO) {
            garmentDao.getAllGarments().associate { it.id to it.toDto() }
        }

        withContext(Dispatchers.Main) {
            entities.forEach { entity ->
                MockDatabase.plannerDays.find { it.day == entity.dayKey }?.let { dayInfo ->
                    dayInfo.topGarment = entity.topGarmentId?.let { allGarments[it] }
                    dayInfo.bottomGarment = entity.bottomGarmentId?.let { allGarments[it] }
                    dayInfo.shoesGarment = entity.shoesGarmentId?.let { allGarments[it] }
                    dayInfo.hatGarment = entity.hatGarmentId?.let { allGarments[it] }
                    dayInfo.accessories = entity.accessoryIds
                        .split(",")
                        .filter { it.isNotBlank() }
                        .mapNotNull { allGarments[it] }
                }
            }
        }
    }
}
