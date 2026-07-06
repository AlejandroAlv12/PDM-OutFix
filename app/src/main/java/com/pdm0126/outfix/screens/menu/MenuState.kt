package com.pdm0126.outfix.screens.menu
import androidx.compose.runtime.*

enum class MenuState {
    MENU,       
    LENT_LIST,  
    ADD_LENT,   
    LENT_DETAIL 
}

object HamburgerMenuState {
    var isOpen by androidx.compose.runtime.mutableStateOf(false)
    var targetLentItem by androidx.compose.runtime.mutableStateOf<com.pdm0126.outfix.data.local.LentItem?>(null)
}