package com.pdm0126.outfix.screens.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdm0126.outfix.data.model.DayInfo
import com.pdm0126.outfix.data.repository.PlannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PlannerViewModel @Inject constructor(
    plannerRepository: PlannerRepository
) : ViewModel() {

    val plannerDays: StateFlow<List<DayInfo>> = plannerRepository.plannerDaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun rotatedDays(days: List<DayInfo>): List<DayInfo> {
        if (days.isEmpty()) return emptyList()
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayIndex = days.indexOfFirst { it.calendarDay == currentDayOfWeek }.takeIf { it >= 0 } ?: 0
        return days.drop(todayIndex) + days.take(todayIndex)
    }
}
