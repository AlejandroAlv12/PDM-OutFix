package com.pdm0126.outfix.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdm0126.outfix.data.local.GarmentDao
import com.pdm0126.outfix.data.prefs.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val garmentDao: GarmentDao
) : ViewModel() {

    val hasSession: StateFlow<Boolean> = sessionManager.sessionState

    fun getUserDisplayName(): String {
        return sessionManager.fetchUserDisplayName() ?: "Usuario"
    }

    fun getUserEmail(): String {
        return sessionManager.fetchUserEmail() ?: "correo@ejemplo.com"
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    garmentDao.deleteAllGarments()
                } catch (e: Exception) {
                }
                sessionManager.clearSession()
            }
            onComplete()
        }
    }
}

