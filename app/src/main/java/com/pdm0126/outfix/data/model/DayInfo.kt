package com.pdm0126.outfix.data.model

import com.pdm0126.outfix.data.api.dto.GarmentResponse
import androidx.compose.ui.graphics.Color

data class DayInfo(
    val day: String,
    val calendarDay: Int,
    var topColor: Color,
    var bottomColor: Color,
    var shoesColor: Color,
    var hatColor: Color?,
    var topGarment: GarmentResponse? = null,
    var bottomGarment: GarmentResponse? = null,
    var shoesGarment: GarmentResponse? = null,
    var hatGarment: GarmentResponse? = null,
    var accessories: List<GarmentResponse> = emptyList()
)
