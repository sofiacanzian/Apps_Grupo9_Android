package com.example.ritmofit.ui.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ritmofit.data.models.AuthState
import com.example.ritmofit.data.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar la autenticación
 */
class AuthViewModel : ViewModel() {

    // Estado de la autenticación
    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    // Campos del formulario
    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _otpCode = mutableStateOf("")
    val otpCode: State<String> = _otpCode

    // Email temporal para el flujo OTP
    private var pendingEmail = ""

    /**
     * Actualizar email
     */
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    /**
     * Actualizar código OTP
     */
    fun updateOtpCode(newCode: String) {
        _otpCode.value = newCode
    }

    /**
     * Enviar código OTP al email
     */
    fun sendOtp() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Validar email
            if (!isValidEmail(_email.value)) {
                _authState.value = AuthState.Error("Por favor ingresa un email válido")
                return@launch
            }

            try {
                // Simular llamada al backend
                delay(1500)

                pendingEmail = _email.value
                _authState.value = AuthState.OtpSent(_email.value)

                // Limpiar el código anterior
                _otpCode.value = ""

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error al enviar código. Intenta nuevamente.")
            }
        }
    }

    /**
     * Verificar código OTP y hacer login
     */
    fun verifyOtp() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Validar código
            if (_otpCode.value.length != 6) {
                _authState.value = AuthState.Error("El código debe tener 6 dígitos")
                return@launch
            }

            try {
                // Simular verificación (código válido: 123456)
                delay(1000)

                if (_otpCode.value == "123456") {
                    // Login exitoso - crear usuario mock
                    val user = User(
                        id = "user_123",
                        email = pendingEmail,
                        name = "Usuario RitmoFit",
                        isVerified = true
                    )

                    val token = "mock_jwt_token_${System.currentTimeMillis()}"

                    _authState.value = AuthState.Success(user, token)
                } else {
                    _authState.value = AuthState.Error("Código incorrecto. Intenta nuevamente.")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error al verificar código. Intenta nuevamente.")
            }
        }
    }

    /**
     * Reenviar código OTP
     */
    fun resendOtp() {
        if (pendingEmail.isNotEmpty()) {
            _email.value = pendingEmail
            sendOtp()
        }
    }

    /**
     * Volver al paso del email
     */
    fun backToEmail() {
        _authState.value = AuthState.Idle
        _otpCode.value = ""
    }

    /**
     * Limpiar errores
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    /**
     * Validar formato de email
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}