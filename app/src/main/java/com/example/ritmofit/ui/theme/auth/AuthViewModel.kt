package com.example.ritmofit.ui.theme.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    var email by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    var showOtpField by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var loginSuccess by mutableStateOf(false)
        private set

    fun updateEmail(newEmail: String) {
        email = newEmail
    }

    fun updateOtp(newOtp: String) {
        otp = newOtp
    }

    fun sendOtp() {
        viewModelScope.launch {
            isLoading = true
            delay(2000) // Simula el envío del OTP
            showOtpField = true
            isLoading = false
        }
    }

    fun verifyOtp() {
        viewModelScope.launch {
            isLoading = true
            delay(1500) // Simula la verificación del OTP
            // En un caso real, aquí iría la validación del código
            if (otp == "123456") { // Código de prueba
                loginSuccess = true
            } else {
                // Manejar error de OTP incorrecto
            }
            isLoading = false
        }
    }
}