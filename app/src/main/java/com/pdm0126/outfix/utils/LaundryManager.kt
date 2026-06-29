package com.pdm0126.outfix.utils

import android.content.Context
import com.pdm0126.outfix.OutfixApplication
import com.pdm0126.outfix.data.api.dto.GarmentResponse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

object LaundryManager {

    private const val PREFS_NAME = "outfix_laundry_prefs"
    private const val KEY_LAST_EVALUATED_DAY = "last_evaluated_day"


    suspend fun evaluatePassedDays(context: Context) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentMillis = System.currentTimeMillis()
        
        val lastEvalMillis = prefs.getLong(KEY_LAST_EVALUATED_DAY, currentMillis)
        
        val lastCal = Calendar.getInstance().apply { timeInMillis = lastEvalMillis }
        val currentCal = Calendar.getInstance().apply { timeInMillis = currentMillis }
        
        lastCal.set(Calendar.HOUR_OF_DAY, 0)
        lastCal.set(Calendar.MINUTE, 0)
        lastCal.set(Calendar.SECOND, 0)
        lastCal.set(Calendar.MILLISECOND, 0)
        
        val todayCal = Calendar.getInstance().apply { timeInMillis = currentMillis }
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)
        
        val diffMillis = todayCal.timeInMillis - lastCal.timeInMillis
        val daysPassed = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

        if (daysPassed > 0) {
            val repository = OutfixApplication.instance.plannerRepository
            val garmentRepo = OutfixApplication.instance.garmentRepository
            
            val daysToProcess = minOf(daysPassed, 7)
            
            for (i in 1..daysToProcess) {
                val evalCal = Calendar.getInstance().apply { 
                    timeInMillis = todayCal.timeInMillis
                    add(Calendar.DAY_OF_YEAR, -i)
                }
                
                val calendarDayOfWeek = evalCal.get(Calendar.DAY_OF_WEEK)
                
                val entities = withContext(Dispatchers.IO) { OutfixApplication.instance.database.plannerDayDao().getAll() }
                val allGarments = withContext(Dispatchers.IO) { OutfixApplication.instance.database.garmentDao().getAllGarments().associateBy { it.id } }
                
                val dayKey = when(calendarDayOfWeek) {
                    Calendar.MONDAY -> "LUN"
                    Calendar.TUESDAY -> "MAR"
                    Calendar.WEDNESDAY -> "MIE"
                    Calendar.THURSDAY -> "JUE"
                    Calendar.FRIDAY -> "VIE"
                    Calendar.SATURDAY -> "SAB"
                    Calendar.SUNDAY -> "DOM"
                    else -> "LUN"
                }

                val dayEntity = entities.find { it.dayKey == dayKey }
                
                if (dayEntity != null) {
                    val garmentsToWash = mutableListOf<GarmentResponse>()
                    val usedDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(evalCal.time)
                    
                    dayEntity.topGarmentId?.let { id -> 
                        allGarments[id]?.toDto()?.let { garmentsToWash.add(it.copy(notes = "USED:$usedDateStr")) } 
                    }
                    dayEntity.bottomGarmentId?.let { id -> 
                        allGarments[id]?.toDto()?.let { garmentsToWash.add(it.copy(notes = "USED:$usedDateStr")) } 
                    }
                    
                    if (garmentsToWash.isNotEmpty()) {
                        garmentRepo.sendToLaundry(garmentsToWash)
                    }
                    repository.clearDayOutfit(dayKey)
                }
            }
            
            prefs.edit().putLong(KEY_LAST_EVALUATED_DAY, currentMillis).apply()
        } else {
            prefs.edit().putLong(KEY_LAST_EVALUATED_DAY, currentMillis).apply()
        }
    }
}
