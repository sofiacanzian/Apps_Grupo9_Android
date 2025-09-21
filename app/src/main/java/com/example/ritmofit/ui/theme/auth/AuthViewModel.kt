package com.example.ritmofit.ui.theme.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ritmofit.RitmoFitApplication
import com.example.ritmofit.data.models.AuthRequest
import com.example.ritmofit.data.models.OtpConfirmationRequest
import com.example.ritmofit.data.models.SessionManager
import com.example.ritmofit.data.models.UserResponse
import com.example.ritmofit.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class AuthViewModel(
    private val apiService: ApiService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginResult = MutableStateFlow<Boolean?>(null)
    val loginResult: StateFlow<Boolean?> = _loginResult.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(SessionManager.isLoggedIn)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setOtp(newOtp: String) {
        _otp.value = newOtp
    }

    fun sendOtp(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginResult.value = null
            _errorMessage.value = null
            try {
                val response = apiService.sendOtp(AuthRequest(email))
                if (response.isSuccessful) {
                    _loginResult.value = true
                } else {
                    _loginResult.value = false
                    _errorMessage.value = "Error al enviar el OTP. Intente de nuevo."
                }
            } catch (e: Exception) {
                _loginResult.value = false
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmOtp(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.confirmOtp(OtpConfirmationRequest(email, _otp.value))
                if (response.isSuccessful) {
                    val userResponse: UserResponse? = response.body()
                    if (userResponse != null && userResponse.user != null) {
                        SessionManager.userId = userResponse.user.id
                        _isAuthenticated.value = true
                    } else {
                        _errorMessage.value = "Respuesta de autenticación nula o sin usuario."
                    }
                } else {
                    _errorMessage.value = "Código OTP incorrecto."
                }
            } catch (e: IOException) {
                _errorMessage.value = "Error de red: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    return AuthViewModel(
                        (application as RitmoFitApplication).container.apiService
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}