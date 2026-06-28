package com.pdm0126.outfix.utils

import android.content.Context
import com.pdm0126.outfix.OutfixApplication
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.mock.MockDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

object LaundryManager {

    private const val PREFS_NAME = "outfix_laundry_prefs"
    private const val KEY_LAST_EVALUATED_DAY = "last_evaluated_day"

    /**
     * Chequea si han pasado mediasnoches desde la ultima evaluacion.
     * Si es asi, busca los outfits de los dias pasados y mueve sus prendas a la lavanderia.
     */
    suspend fun evaluatePassedDays(context: Context) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentMillis = System.currentTimeMillis()
        
        // Usa current millis si es la primera vez que se ejecuta
        val lastEvalMillis = prefs.getLong(KEY_LAST_EVALUATED_DAY, currentMillis)
        
        val lastCal = Calendar.getInstance().apply { timeInMillis = lastEvalMillis }
        val currentCal = Calendar.getInstance().apply { timeInMillis = currentMillis }
        
        // Calcula la diferencia en dias (ignorando la hora del dia)
        // Reinicia la hora a 00:00:00 para contar correctamente los cruces de medianoche
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
            
            // Itera sobre cada dia que paso (hasta 7 dias maximo para prevenir bucles infinitos si el usuario no abrio la app en meses)
            val daysToProcess = minOf(daysPassed, 7)
            
            for (i in 1..daysToProcess) {
                // Determina el dia de la semana que paso
                val evalCal = Calendar.getInstance().apply { 
                    timeInMillis = todayCal.timeInMillis
                    add(Calendar.DAY_OF_YEAR, -i)
                }
                
                val calendarDayOfWeek = evalCal.get(Calendar.DAY_OF_WEEK)
                
                // Encuentra el DayInfo correspondiente en MockDatabase (que actúa como nuestro mapa de memoria para los días)
                val dayInfo = MockDatabase.plannerDays.find { it.calendarDay == calendarDayOfWeek }
                
                if (dayInfo != null) {
                    // Extrae las prendas
                    val garmentsToWash = mutableListOf<GarmentResponse>()
                    dayInfo.topGarment?.let { garmentsToWash.add(it) }
                    dayInfo.bottomGarment?.let { garmentsToWash.add(it) }
                    dayInfo.shoesGarment?.let { garmentsToWash.add(it) }
                    dayInfo.hatGarment?.let { garmentsToWash.add(it) }
                    garmentsToWash.addAll(dayInfo.accessories)
                    
                    if (garmentsToWash.isNotEmpty()) {
                        // Marca como lavanderia
                        garmentRepo.sendToLaundry(garmentsToWash)
                        
                        // Limpia el dia del planner
                        repository.clearDayOutfit(dayInfo.day)
                    }
                }
            }
            
            // Actualiza la ultima fecha evaluada
            prefs.edit().putLong(KEY_LAST_EVALUATED_DAY, currentMillis).apply()
        } else {
            // La primera vez que se ejecuta, guarda la fecha actual
            prefs.edit().putLong(KEY_LAST_EVALUATED_DAY, currentMillis).apply()
        }
    }
}
