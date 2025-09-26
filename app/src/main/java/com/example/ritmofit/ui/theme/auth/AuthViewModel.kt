package com.example.ritmofit.ui.theme.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ritmofit.RitmoFitApplication
import com.example.ritmofit.data.models.AuthRequest
import com.example.ritmofit.data.models.OtpConfirmationRequest
import com.example.ritmofit.data.models.PasswordResetRequest
import com.example.ritmofit.data.models.RegistrationRequest
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

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Usa el StateFlow de SessionManager para que la UI reaccione a los cambios de sesión
    val isAuthenticated: StateFlow<Boolean> = SessionManager.isLoggedIn

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    fun setOtp(newOtp: String) {
        if (newOtp.length <= 6 && newOtp.all { it.isDigit() }) {
            _otp.value = newOtp
        }
    }

    fun confirmOtp(email: String, nextAction: String) {
        verifyOtp(email, _otp.value, nextAction)
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    fun registerAndSendOtp(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            _otp.value = ""
            try {
                val response = apiService.registerAndSendOtp(RegistrationRequest(email, password))
                if (response.isSuccessful) {
                    _successMessage.value = "OTP enviado para registro"
                    onSuccess()
                } else {
                    _errorMessage.value = "Error al registrar: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de red: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginAndSendOtp(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            _otp.value = ""
            try {
                val response = apiService.loginAndSendOtp(AuthRequest(email, password = password))
                if (response.isSuccessful) {
                    _successMessage.value = "OTP enviado para login"
                    onSuccess()
                } else {
                    _errorMessage.value = "Credenciales incorrectas o error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun verifyOtp(email: String, otpCode: String, nextAction: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                val request = OtpConfirmationRequest(email, otpCode)
                val response = apiService.verifyOtpAndLogin(request)

                if (response.isSuccessful) {
                    val userResponse: UserResponse? = response.body()

                    if (userResponse?.token != null) {
                        // Llama a la función suspend de SessionManager para guardar el token
                        SessionManager.setSession(userResponse.token, userResponse.user.id)
                        _otp.value = ""
                        _successMessage.value = when (nextAction) {
                            "REGISTER" -> "Usuario registrado con éxito"
                            "LOGIN" -> "Sesión iniciada con éxito"
                            else -> "Verificación exitosa"
                        }
                    } else {
                        _errorMessage.value = "Error en la respuesta de autenticación: Token faltante."
                    }
                } else {
                    _errorMessage.value = "Código OTP incorrecto."
                }
            } catch (e: IOException) {
                _errorMessage.value = "Error de red: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}. Asegúrate que el backend devuelva el campo 'token'."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestPasswordResetOtp(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            _otp.value = ""
            try {
                val response = apiService.requestPasswordResetOtp(AuthRequest(email = email))
                if (response.isSuccessful) {
                    _successMessage.value = "OTP enviado para recuperación"
                    onSuccess()
                } else {
                    _errorMessage.value = "Email no encontrado o error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword(email: String, newPassword: String, onPasswordResetSuccess: () -> Unit) {
        val otpCode = _otp.value
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                val request = PasswordResetRequest(email, newPassword, otpCode)
                val response = apiService.resetPassword(request)

                if (response.isSuccessful) {
                    _successMessage.value = "Contraseña restablecida con éxito"
                    _otp.value = ""
                    onPasswordResetSuccess()
                } else {
                    _errorMessage.value = "Error al restablecer la contraseña. (OTP incorrecto o expirado)."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Llama a la función suspend de SessionManager para cerrar la sesión
            SessionManager.logout()
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