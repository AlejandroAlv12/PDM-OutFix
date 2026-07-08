package com.pdm0126.outfix.screens.closet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.model.DayInfo
import com.pdm0126.outfix.data.repository.GarmentRepository
import com.pdm0126.outfix.data.repository.PlannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ClosetUiState(
    val selectedTop: GarmentResponse? = null,
    val selectedBottom: GarmentResponse? = null,
    val selectedShoes: GarmentResponse? = null,
    val selectedHead: GarmentResponse? = null,
    val selectedAccessories: List<GarmentResponse> = emptyList(),
    val originalTop: GarmentResponse? = null,
    val originalBottom: GarmentResponse? = null,
    val originalShoes: GarmentResponse? = null,
    val originalHead: GarmentResponse? = null,
    val originalAccessories: List<GarmentResponse> = emptyList(),
    val isLoading: Boolean = false,
)

val ClosetUiState.hasChanges: Boolean
    get() = selectedTop != originalTop ||
            selectedBottom != originalBottom ||
            selectedShoes != originalShoes ||
            selectedHead != originalHead ||
            selectedAccessories != originalAccessories

@HiltViewModel
class ClosetViewModel @Inject constructor(
    private val garmentRepository: GarmentRepository,
    private val plannerRepository: PlannerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClosetUiState())
    val uiState: StateFlow<ClosetUiState> = _uiState.asStateFlow()

    val garments: StateFlow<List<GarmentResponse>> = garmentRepository.availableGarmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val plannerDays: StateFlow<List<DayInfo>> = plannerRepository.plannerDaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refreshGarments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            garmentRepository.refreshGarments()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectTop(garment: GarmentResponse?) {
        _uiState.update { state ->
            val newTop = if (state.selectedTop == garment) null else garment
            val newBottom = if (newTop?.category?.equals("Vestido", ignoreCase = true) == true) null
                           else state.selectedBottom
            state.copy(selectedTop = newTop, selectedBottom = newBottom)
        }
    }

    fun selectBottom(garment: GarmentResponse?) {
        _uiState.update { it.copy(selectedBottom = if (it.selectedBottom == garment) null else garment) }
    }

    fun selectShoes(garment: GarmentResponse?) {
        _uiState.update { it.copy(selectedShoes = if (it.selectedShoes == garment) null else garment) }
    }

    fun selectHead(garment: GarmentResponse?) {
        _uiState.update { it.copy(selectedHead = if (it.selectedHead == garment) null else garment) }
    }

    fun toggleAccessory(garment: GarmentResponse) {
        _uiState.update { state ->
            val isAlreadySelected = state.selectedAccessories.contains(garment)
            val newAccessories = if (isAlreadySelected) {
                state.selectedAccessories.filter { it.id != garment.id }
            } else {
                val exclusiveBags = listOf("bolso", "mochila")
                val isBag = garment.category?.lowercase() in exclusiveBags
                val filtered = state.selectedAccessories.filter { existing ->
                    val sameCategory = existing.category.equals(garment.category, ignoreCase = true)
                    val bothAreBags = isBag && existing.category?.lowercase() in exclusiveBags
                    !(sameCategory || bothAreBags)
                }
                filtered + garment
            }
            state.copy(selectedAccessories = newAccessories)
        }
    }

    fun loadOutfitForDay(dayName: String?, plannerDays: List<DayInfo>) {
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val dayInfo = if (dayName != null) {
            plannerDays.find { it.day == dayName }
        } else {
            plannerDays.find { it.calendarDay == currentDayOfWeek }
        }
        if (dayInfo != null) {
            _uiState.update {
                it.copy(
                    selectedTop = dayInfo.topGarment,
                    selectedBottom = dayInfo.bottomGarment,
                    selectedShoes = dayInfo.shoesGarment,
                    selectedHead = dayInfo.hatGarment,
                    selectedAccessories = dayInfo.accessories,
                    originalTop = dayInfo.topGarment,
                    originalBottom = dayInfo.bottomGarment,
                    originalShoes = dayInfo.shoesGarment,
                    originalHead = dayInfo.hatGarment,
                    originalAccessories = dayInfo.accessories,
                )
            }
        }
    }

    fun saveOutfit(plannerEditDay: String?, plannerDays: List<DayInfo>) {
        val state = _uiState.value
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val targetDay = plannerEditDay
            ?: plannerDays.find { it.calendarDay == currentDayOfWeek }?.day
            ?: "Lunes"

        viewModelScope.launch {
            plannerRepository.saveDayOutfit(
                dayKey = targetDay,
                top = state.selectedTop,
                bottom = state.selectedBottom,
                shoes = state.selectedShoes,
                head = state.selectedHead,
                accessories = state.selectedAccessories
            )
        }

        if (plannerEditDay == null) {
            _uiState.update {
                it.copy(
                    originalTop = it.selectedTop,
                    originalBottom = it.selectedBottom,
                    originalShoes = it.selectedShoes,
                    originalHead = it.selectedHead,
                    originalAccessories = it.selectedAccessories,
                )
            }
        }
    }

    fun randomizeOutfit(tops: List<GarmentResponse>, bottoms: List<GarmentResponse>, shoes: List<GarmentResponse>) {
        val randomTop = tops.filter { it.status != "IN_WASH" }.randomOrNull() ?: return
        val randomBottom = if (randomTop.category.equals("Vestido", ignoreCase = true)) null
                          else bottoms.filter { it.status != "IN_WASH" }.let {
                              val matching = it.filter { b -> b.style == randomTop.style }
                              if (matching.isNotEmpty()) matching.random() else it.randomOrNull()
                          }
        val randomShoes = shoes.filter { it.status != "IN_WASH" }.let {
            val matching = it.filter { s -> s.style == randomTop.style }
            if (matching.isNotEmpty()) matching.random() else it.randomOrNull()
        }
        _uiState.update {
            it.copy(selectedTop = randomTop, selectedBottom = randomBottom, selectedShoes = randomShoes)
        }
    }
}
