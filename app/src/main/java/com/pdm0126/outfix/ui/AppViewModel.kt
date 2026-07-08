package com.pdm0126.outfix.ui

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.local.LentItem
import com.pdm0126.outfix.data.model.DayInfo
import com.pdm0126.outfix.data.repository.GarmentRepository
import com.pdm0126.outfix.data.repository.PlannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class AppUiState(
    val detailGarment: GarmentResponse? = null,
    val detailGarmentBounds: Rect? = null,
    val isGarmentOverlayActive: Boolean = false,

    val detailDayInfo: DayInfo? = null,
    val detailDayBounds: Rect? = null,
    val detailDayTextBounds: Rect? = null,
    val detailDayCharBounds: Rect? = null,
    val isDayOverlayActive: Boolean = false,
    val isHomeOverlayActive: Boolean = false,
    val homeOverlayBounds: Rect? = null,
    val homeHoyBounds: Rect? = null,
    val homeCharBounds: Rect? = null,
    val homeStyleBounds: Rect? = null,
    val homeDayInfo: DayInfo? = null,

    val isLaundryOverlayActive: Boolean = false,
    val laundryOverlayBounds: Rect? = null,
    val laundryButtonBounds: Rect? = null,
    val laundryGarment: GarmentResponse? = null,
    val laundryTitleText: String = "",
    val laundrySubtitleText: String = "",
    val isHamburgerOpen: Boolean = false,
    val targetLentItem: LentItem? = null,
    val isFabVisible: Boolean = true,
    val plannerEditDay: String? = null,
    val hasLoadedPlannerDay: Boolean = false,
    
    val isSplashFinished: Boolean = false
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val garmentRepository: GarmentRepository,
    private val plannerRepository: PlannerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _navigationRequest = MutableSharedFlow<OutFixScreen>(extraBufferCapacity = 1)
    val navigationRequest = _navigationRequest.asSharedFlow()

    fun showGarmentDetail(garment: GarmentResponse, bounds: Rect?) {
        _uiState.update { it.copy(detailGarment = garment, detailGarmentBounds = bounds, isGarmentOverlayActive = true) }
    }

    fun dismissGarmentOverlay() {
        _uiState.update { it.copy(isGarmentOverlayActive = false) }
    }

    fun clearGarmentDetail() {
        _uiState.update { it.copy(detailGarment = null, detailGarmentBounds = null) }
    }

    fun updateGarment(garment: GarmentResponse) {
        dismissGarmentOverlay()
        viewModelScope.launch { garmentRepository.updateGarment(garment) }
    }

    fun deleteGarment(garmentId: String) {
        dismissGarmentOverlay()
        viewModelScope.launch { garmentRepository.deleteGarment(garmentId) }
    }

    fun markAsWashed(garmentId: String) {
        viewModelScope.launch { garmentRepository.markAsWashed(garmentId) }
    }

    fun showDayDetail(dayInfo: DayInfo, bounds: Rect?, textBounds: Rect? = null, charBounds: Rect? = null) {
        _uiState.update { it.copy(detailDayInfo = dayInfo, detailDayBounds = bounds, detailDayTextBounds = textBounds, detailDayCharBounds = charBounds, isDayOverlayActive = true) }
    }

    fun dismissDayOverlay() {
        _uiState.update { it.copy(isDayOverlayActive = false) }
    }

    fun showHomeOverlay(
        dayInfo: DayInfo,
        bounds: Rect?,
        hoyBounds: Rect?,
        charBounds: Rect?,
        styleBounds: Rect?
    ) {
        _uiState.update {
            it.copy(
                homeDayInfo = dayInfo,
                homeOverlayBounds = bounds,
                homeHoyBounds = hoyBounds,
                homeCharBounds = charBounds,
                homeStyleBounds = styleBounds,
                isHomeOverlayActive = true
            )
        }
    }

    fun dismissHomeOverlay() {
        _uiState.update { it.copy(isHomeOverlayActive = false) }
    }

    fun setHomeOverlayBounds(bounds: Rect?) {
        _uiState.update { it.copy(homeOverlayBounds = bounds) }
    }

    fun showLaundryOverlay(
        garment: GarmentResponse,
        bounds: Rect?,
        buttonBounds: Rect?,
        titleText: String,
        subtitleText: String
    ) {
        _uiState.update {
            it.copy(
                laundryGarment = garment,
                laundryOverlayBounds = bounds,
                laundryButtonBounds = buttonBounds,
                laundryTitleText = titleText,
                laundrySubtitleText = subtitleText,
                isLaundryOverlayActive = true
            )
        }
    }

    fun dismissLaundryOverlay() {
        _uiState.update { it.copy(isLaundryOverlayActive = false) }
    }

    fun openHamburgerMenu(targetLentItem: LentItem? = null) {
        _uiState.update { it.copy(isHamburgerOpen = true, targetLentItem = targetLentItem) }
    }

    fun closeHamburgerMenu() {
        _uiState.update { it.copy(isHamburgerOpen = false, targetLentItem = null) }
    }

    fun clearTargetLentItem() {
        _uiState.update { it.copy(targetLentItem = null) }
    }

    fun setFabVisible(visible: Boolean) {
        _uiState.update { it.copy(isFabVisible = visible) }
    }

    fun setPlannerEditDay(day: String?) {
        _uiState.update { it.copy(plannerEditDay = day, hasLoadedPlannerDay = false) }
    }

    fun markPlannerDayLoaded() {
        _uiState.update { it.copy(hasLoadedPlannerDay = true) }
    }

    fun resetPlannerDayLoaded() {
        _uiState.update { it.copy(hasLoadedPlannerDay = false) }
    }

    fun setSplashFinished() {
        _uiState.update { it.copy(isSplashFinished = true) }
    }
    fun navigateTo(screen: OutFixScreen) {
        viewModelScope.launch { _navigationRequest.emit(screen) }
    }

    fun initializeApp() {
        viewModelScope.launch {
            plannerRepository.restorePlannerDays()
        }
    }

    fun evaluatePassedDays(context: android.content.Context) {
        viewModelScope.launch {
            com.pdm0126.outfix.utils.LaundryManager.evaluatePassedDays(context, plannerRepository, garmentRepository)
        }
    }
}
