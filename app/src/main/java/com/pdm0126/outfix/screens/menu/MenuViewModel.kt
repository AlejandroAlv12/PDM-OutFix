package com.pdm0126.outfix.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.local.LentItem
import com.pdm0126.outfix.data.repository.GarmentRepository
import com.pdm0126.outfix.data.repository.LentRepository
import com.pdm0126.outfix.data.repository.PlannerRepository
import com.pdm0126.outfix.data.model.DayInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val lentRepository: LentRepository,
    private val garmentRepository: GarmentRepository,
    private val plannerRepository: PlannerRepository
) : ViewModel() {

    val lentItems: StateFlow<List<LentItem>> = lentRepository.lentItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val garments: StateFlow<List<GarmentResponse>> = garmentRepository.garmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val plannerDays: StateFlow<List<DayInfo>> = plannerRepository.plannerDaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _menuState = MutableStateFlow(MenuState.MENU)
    val menuState: StateFlow<MenuState> = _menuState.asStateFlow()

    private val _selectedLentItem = MutableStateFlow<LentItem?>(null)
    val selectedLentItem: StateFlow<LentItem?> = _selectedLentItem.asStateFlow()

    fun navigateTo(state: MenuState) {
        _menuState.update { state }
    }

    fun selectLentItem(item: LentItem?) {
        _selectedLentItem.update { item }
    }

    fun refresh() {
        viewModelScope.launch { lentRepository.refresh() }
    }

    fun createLentItem(
        garmentId: String,
        garmentImageUrl: String?,
        garmentName: String,
        borrowerName: String,
        lentDate: String,
        reclaimDate: String,
        reclaimDateMillis: Long,
        onCreated: (LentItem) -> Unit
    ) {
        viewModelScope.launch {
            val newItem = lentRepository.createLentItem(
                garmentId = garmentId,
                garmentImageUrl = garmentImageUrl,
                garmentName = garmentName,
                borrowerName = borrowerName,
                lentDate = lentDate,
                reclaimDate = reclaimDate,
                reclaimDateMillis = reclaimDateMillis
            )
            val garment = garments.value.find { it.id == garmentId }
            if (garment != null) {
                garmentRepository.updateGarment(garment.copy(status = "LENT"))
            }
            onCreated(newItem)
            _menuState.update { MenuState.LENT_LIST }
        }
    }

    fun markReturned(item: LentItem, onDone: () -> Unit) {
        viewModelScope.launch {
            lentRepository.markReturned(item.id)
            val garment = garments.value.find { it.id == item.garmentId }
            if (garment != null) {
                garmentRepository.updateGarment(garment.copy(status = "AVAILABLE"))
            }
            onDone()
            _menuState.update { MenuState.LENT_LIST }
        }
    }

    fun deleteLentItem(id: String) {
        viewModelScope.launch { lentRepository.deleteLentItem(id) }
    }
}
