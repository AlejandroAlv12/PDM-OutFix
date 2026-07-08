package com.pdm0126.outfix.screens.laundry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.model.DayInfo
import com.pdm0126.outfix.data.repository.GarmentRepository
import com.pdm0126.outfix.data.repository.PlannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaundryViewModel @Inject constructor(
    private val garmentRepository: GarmentRepository,
    private val plannerRepository: PlannerRepository
) : ViewModel() {

    val garments: StateFlow<List<GarmentResponse>> = garmentRepository.garmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val plannerDays: StateFlow<List<DayInfo>> = plannerRepository.plannerDaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markAsWashed(garmentId: String) {
        viewModelScope.launch {
            garmentRepository.markAsWashed(garmentId)
        }
    }

    fun markAllWashed(garments: List<GarmentResponse>) {
        viewModelScope.launch {
            garments.forEach { garmentRepository.markAsWashed(it.id) }
        }
    }
}
