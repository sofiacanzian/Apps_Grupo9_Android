// Archivo: AuthViewModel.kt (CORREGIDO)
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

    private val _isAuthenticated = MutableStateFlow(SessionManager.isLoggedIn)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- ESTADOS ESPECÍFICOS PARA OTP (AÑADIDOS/MODIFICADOS) ---
    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    fun setOtp(newOtp: String) {
        // Lógica para limitar a 6 dígitos y aceptar solo números
        if (newOtp.length <= 6 && newOtp.all { it.isDigit() }) {
            _otp.value = newOtp
        }
    }

    // Función de la pantalla que llama a la lógica de verificación
    fun confirmOtp(email: String, nextAction: String) {
        verifyOtp(email, _otp.value, nextAction)
    }
    // -----------------------------------------------------------

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    // ----------------------------------------------------
    // 1. REGISTRO Y ENVÍO DE OTP
    // ----------------------------------------------------
    fun registerAndSendOtp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            _otp.value = "" // Limpiar OTP al iniciar un nuevo flujo
            try {
                val response = apiService.registerAndSendOtp(RegistrationRequest(email, password))
                if (response.isSuccessful) {
                    _successMessage.value = "OTP enviado para registro"
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

    // ----------------------------------------------------
    // 2. LOGIN Y ENVÍO DE OTP
    // ----------------------------------------------------
    fun loginAndSendOtp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            _otp.value = "" // Limpiar OTP al iniciar un nuevo flujo
            try {
                val response = apiService.loginAndSendOtp(AuthRequest(email, password))
                if (response.isSuccessful) {
                    _successMessage.value = "OTP enviado para login"
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

    // ----------------------------------------------------
    // 3. VERIFICACIÓN DE OTP (Lógica de red, ahora privada)
    // ----------------------------------------------------
    private fun verifyOtp(email: String, otpCode: String, nextAction: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                val response = apiService.verifyOtpAndLogin(OtpConfirmationRequest(email, otpCode))
                if (response.isSuccessful) {
                    val userResponse: UserResponse? = response.body()
                    if (userResponse?.token != null) {
                        SessionManager.userId = userResponse.user.id
                        // SessionManager.token = userResponse.token
                        _isAuthenticated.value = true

                        _successMessage.value = when (nextAction) {
                            "REGISTER" -> "Usuario registrado con éxito"
                            "LOGIN" -> "Sesión iniciada con éxito"
                            "PASSWORD_RESET" -> "OTP verificado para recuperación"
                            else -> "Verificación exitosa"
                        }
                    } else {
                        _errorMessage.value = "Error en la respuesta de autenticación."
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

    // ----------------------------------------------------
    // 4. RECUPERACIÓN DE CONTRASEÑA (SOLICITUD)
    // ----------------------------------------------------
    fun requestPasswordResetOtp(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            _otp.value = "" // Limpiar OTP al iniciar un nuevo flujo
            try {
                val response = apiService.requestPasswordResetOtp(AuthRequest(email = email))
                if (response.isSuccessful) {
                    _successMessage.value = "OTP enviado para recuperación"
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

    // ----------------------------------------------------
    // 5. RESTABLECIMIENTO DE CONTRASEÑA (FINAL)
    // ----------------------------------------------------
    fun resetPassword(email: String, newPassword: String) {
        // Obtiene el OTP del StateFlow interno
        val otpCode = _otp.value

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                val response = apiService.resetPassword(PasswordResetRequest(email, newPassword, otpCode))
                if (response.isSuccessful) {
                    _successMessage.value = "Contraseña restablecida con éxito"
                } else {
                    _errorMessage.value = "Error al restablecer la contraseña."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
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