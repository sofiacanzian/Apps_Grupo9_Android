// Archivo: AuthViewModel.kt
package com.example.ritmofit.ui.theme.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ritmofit.data.models.AuthRequest
import com.example.ritmofit.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.example.ritmofit.data.models.OtpResponse
import com.example.ritmofit.data.models.User
import java.lang.Exception

class AuthViewModel : ViewModel() {

    sealed class NavigationEvent {
        data class NavigateToOtp(val email: String) : NavigationEvent()
        object NavigateToHome : NavigationEvent()
    }

    private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Initial)
    val loginResult: StateFlow<LoginResult> = _loginResult

    sealed class LoginResult {
        object Initial : LoginResult()
        data class Success(val email: String) : LoginResult()
        data class Error(val message: String) : LoginResult()
        object OtpConfirmed : LoginResult()
    }

    fun sendOtp(email: String) {
        _isLoading.value = true
        _loginResult.value = LoginResult.Initial
        viewModelScope.launch {
            try {
                // Aquí, el ViewModel ahora espera un objeto Response de la API
                val response = RetrofitClient.apiService.sendOtp(AuthRequest(email))

                // Verificamos si la respuesta fue exitosa y si tiene un cuerpo
                if (response.isSuccessful) {
                    val otpResponse = response.body()
                    if (otpResponse != null && otpResponse.message.contains("Código OTP enviado")) {
                        _loginResult.value = LoginResult.Success(email)
                        _navigationEvents.send(NavigationEvent.NavigateToOtp(email))
                    } else {
                        _loginResult.value = LoginResult.Error("Respuesta inesperada del servidor")
                    }
                } else {
                    _loginResult.value = LoginResult.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult.Error("Error al enviar el código: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmOtp(email: String, otp: String) {
        _isLoading.value = true
        _loginResult.value = LoginResult.Initial
        viewModelScope.launch {
            try {
                // Hacemos lo mismo para la función verifyOtp
                val response = RetrofitClient.apiService.verifyOtp(AuthRequest(email, otp))

                if (response.isSuccessful) {
                    val otpResponse = response.body()
                    if (otpResponse != null && otpResponse.message.contains("verificado exitosamente")) {
                        _loginResult.value = LoginResult.OtpConfirmed
                        _navigationEvents.send(NavigationEvent.NavigateToHome)
                    } else {
                        _loginResult.value = LoginResult.Error("Respuesta inesperada del servidor")
                    }
                } else {
                    _loginResult.value = LoginResult.Error("Código incorrecto o expirado")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult.Error("Código incorrecto o expirado")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _loginResult.value = LoginResult.Initial
    }
}