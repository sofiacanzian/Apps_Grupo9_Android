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

    // Estado para capturar el OTP de la UI (usado en OtpVerification y ResetPassword)
    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    /**
     * Asegura que el OTP se actualice correctamente en el estado
     * al permitir cambios de entrada, incluyendo el borrado, y aplicando un límite de 6 dígitos.
     */
    fun setOtp(newOtp: String) {
        val filteredOtp = newOtp.filter { it.isDigit() }
        if (filteredOtp.length <= 6) {
            _otp.value = filteredOtp
        }
    }

    /**
     * Agregamos un callback 'onSuccess' a confirmOtp para permitir
     * la navegación específica (como pasar a ResetPassword) después de la verificación exitosa.
     */
    fun confirmOtp(email: String, nextAction: String, onSuccess: () -> Unit = {}) {
        verifyOtp(email, _otp.value, nextAction, onSuccess)
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
            clearMessages()
            _otp.value = "" // Limpia el OTP
            try {
                val response = apiService.registerAndSendOtp(RegistrationRequest(email, password))
                if (response.isSuccessful) {
                    _successMessage.value = "Código de verificación enviado a $email"
                    onSuccess()
                } else {
                    // Intenta obtener un mensaje de error más específico si es posible
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "Error al registrar. Por favor, verifica tu email. Detalle: ${errorBody ?: response.code().toString()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de red: No se pudo conectar con el servidor."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginAndSendOtp(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            clearMessages()
            _otp.value = "" // Limpia el OTP
            try {
                val response = apiService.loginAndSendOtp(AuthRequest(email, password = password))
                if (response.isSuccessful) {
                    _successMessage.value = "Código de verificación enviado a $email"
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "Credenciales incorrectas o usuario no verificado."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: No se pudo conectar con el servidor."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ Lógica corregida para el reseteo de contraseña:
     * En el caso de "RESET_PASSWORD", solo verificamos la validez del OTP,
     * pero NO llamamos a SessionManager.setSession() para evitar iniciar sesión
     * y potencialmente invalidar el OTP para el paso final de reseteo.
     */
    private fun verifyOtp(email: String, otpCode: String, nextAction: String, onVerificationSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            clearMessages()
            try {
                val request = OtpConfirmationRequest(email, otpCode)
                val response = apiService.verifyOtpAndLogin(request)

                if (response.isSuccessful) {
                    val userResponse: UserResponse? = response.body()

                    // Manejo del flujo
                    if (nextAction == "RESET_PASSWORD") {
                        // Flujo de reseteo: El OTP es válido. No guardamos sesión.
                        _successMessage.value = "Código verificado. Procede a ingresar tu nueva contraseña."
                        onVerificationSuccess() // Navega a ResetPasswordScreen

                    } else if (userResponse?.token != null && userResponse.user.id.isNotEmpty()) {
                        // Flujo normal de LOGIN/REGISTER: Guardamos sesión.
                        SessionManager.setSession(userResponse.token, userResponse.user.id)
                        _otp.value = ""
                        _successMessage.value = when (nextAction) {
                            "REGISTER" -> "Registro y sesión iniciada con éxito."
                            "LOGIN" -> "Sesión iniciada con éxito."
                            else -> "Verificación exitosa."
                        }
                    } else {
                        _errorMessage.value = "Error: Respuesta incompleta o token/ID de usuario faltante."
                    }
                } else {
                    _errorMessage.value = "Código OTP incorrecto o ha expirado. Por favor, inténtalo de nuevo."
                }
            } catch (e: IOException) {
                _errorMessage.value = "Error de red: No se pudo conectar con el servidor."
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado durante la verificación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestPasswordResetOtp(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            clearMessages()
            _otp.value = "" // Limpia el OTP antes de solicitar uno nuevo
            try {
                val response = apiService.requestPasswordResetOtp(AuthRequest(email = email))
                if (response.isSuccessful) {
                    _successMessage.value = "OTP de recuperación enviado a $email"
                    onSuccess()
                } else {
                    _errorMessage.value = "Email no encontrado o error en la solicitud."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: No se pudo conectar con el servidor."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Acepta el 'otpCode' explícitamente como argumento y lo envía en el request final.
     */
    fun resetPassword(email: String, newPassword: String, otpCode: String, onPasswordResetSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            clearMessages()
            try {
                val request = PasswordResetRequest(email, newPassword, otpCode)
                val response = apiService.resetPassword(request)

                if (response.isSuccessful) {
                    _successMessage.value = "Contraseña restablecida con éxito. Puedes iniciar sesión."
                    _otp.value = "" // Limpia el OTP al finalizar
                    onPasswordResetSuccess()
                } else {
                    _errorMessage.value = "Error al restablecer la contraseña. El OTP pudo haber expirado o ser incorrecto."
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
            // Opcional: limpiar mensajes después de un logout
            clearMessages()
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