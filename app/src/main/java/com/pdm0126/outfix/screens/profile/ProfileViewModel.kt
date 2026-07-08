package com.pdm0126.outfix.screens.profile

import androidx.lifecycle.ViewModel
import com.pdm0126.outfix.data.prefs.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    val hasSession: StateFlow<Boolean> = sessionManager.sessionState

    fun getUserDisplayName(): String {
        return sessionManager.fetchUserDisplayName() ?: "Usuario"
    }

    fun getUserEmail(): String {
        return sessionManager.fetchUserEmail() ?: "correo@ejemplo.com"
    }
}
