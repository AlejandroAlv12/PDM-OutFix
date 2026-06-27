package com.pdm0126.outfix.screens.closet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pdm0126.outfix.data.api.dto.GarmentResponse

class ClosetViewModel : ViewModel() {
    var selectedTop by mutableStateOf<GarmentResponse?>(null)
    var selectedBottom by mutableStateOf<GarmentResponse?>(null)
    var selectedShoes by mutableStateOf<GarmentResponse?>(null)
    var selectedHead by mutableStateOf<GarmentResponse?>(null)
    var selectedAccessories by mutableStateOf<List<GarmentResponse>>(emptyList())
}
