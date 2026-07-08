package com.pdm0126.outfix.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdm0126.outfix.data.api.AuthApi
import com.pdm0126.outfix.data.api.dto.LoginRequest
import com.pdm0126.outfix.data.api.dto.RegisterRequest
import com.pdm0126.outfix.data.prefs.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.login(request)
                if (response.success && response.data != null) {
                    sessionManager.apply {
                        saveAuthToken(response.data.token)
                        saveUserId(response.data.user.id)
                        saveUserEmail(response.data.user.email)
                        saveUserDisplayName(response.data.user.displayName)
                    }
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error(response.message ?: "Error desconocido")
                }
            } catch (e: HttpException) {
                try {
                    val errorJson = e.response()?.errorBody()?.string()
                    val msg = JSONObject(errorJson ?: "").optString("message", "Error (${e.code()})")
                    _authState.value = AuthState.Error(msg)
                } catch (parseException: Exception) {
                    _authState.value = AuthState.Error("Error (${e.code()})")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error("Error de red")
            }
        }
    }

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.register(request)
                if (response.success && response.data != null) {
                    sessionManager.apply {
                        saveAuthToken(response.data.token)
                        saveUserId(response.data.user.id)
                        saveUserEmail(response.data.user.email)
                        saveUserDisplayName(response.data.user.displayName)
                    }
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error(response.message ?: "Error desconocido")
                }
            } catch (e: HttpException) {
                try {
                    val errorJson = e.response()?.errorBody()?.string()
                    val msg = JSONObject(errorJson ?: "").optString("message", "Error (${e.code()})")
                    _authState.value = AuthState.Error(msg)
                } catch (parseException: Exception) {
                    _authState.value = AuthState.Error("Error (${e.code()})")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error("Error de red")
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
