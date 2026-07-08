package com.pdm0126.outfix.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.local.LentItem
import com.pdm0126.outfix.data.model.DayInfo
import com.pdm0126.outfix.data.repository.GarmentRepository
import com.pdm0126.outfix.data.repository.LentRepository
import com.pdm0126.outfix.data.repository.PlannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val garmentRepository: GarmentRepository,
    private val lentRepository: LentRepository
) : ViewModel() {

    val plannerDays: StateFlow<List<DayInfo>> = plannerRepository.plannerDaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val garments: StateFlow<List<GarmentResponse>> = garmentRepository.garmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lentItems: StateFlow<List<LentItem>> = lentRepository.lentItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun shuffleAndSaveToday(todayInfo: DayInfo?) {
        val currentGarments = garments.value
        val tops = currentGarments.filter {
            it.status == "AVAILABLE" && it.category in listOf(
                "Camiseta", "Camisa", "Blusa", "Top", "Suéter", "Chaqueta", "Abrigo", "Vestido"
            )
        }
        val bottoms = currentGarments.filter {
            it.status == "AVAILABLE" && it.category in listOf("Jeans", "Pantalón", "Short", "Falda")
        }
        val shoes = currentGarments.filter {
            it.status == "AVAILABLE" && it.category in listOf("Zapatillas", "Botas", "Zapatos")
        }

        val randomTop = tops.randomOrNull() ?: return
        val randomBottom = if (randomTop.category.equals("Vestido", ignoreCase = true)) null
                          else bottoms.randomOrNull()
        val randomShoes = shoes.randomOrNull()

        if (todayInfo != null) {
            viewModelScope.launch {
                plannerRepository.saveDayOutfit(
                    dayKey = todayInfo.day,
                    top = randomTop,
                    bottom = randomBottom,
                    shoes = randomShoes,
                    head = todayInfo.hatGarment,
                    accessories = todayInfo.accessories
                )
            }
        }
    }

    fun computeStreak(plannerDays: List<DayInfo>): Int {
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        var count = 0
        var checkDay = currentDayOfWeek
        var isFirstCheck = true
        for (i in 0 until 7) {
            val dayInfo = plannerDays.find { it.calendarDay == checkDay }
            val hasOutfit = dayInfo != null &&
                (dayInfo.topGarment != null || dayInfo.bottomGarment != null ||
                 dayInfo.shoesGarment != null || dayInfo.hatGarment != null)
            if (hasOutfit) {
                count++
            } else if (!isFirstCheck) {
                break
            }
            isFirstCheck = false
            checkDay = if (checkDay == Calendar.SUNDAY) Calendar.SATURDAY else checkDay - 1
        }
        return count
    }
}
