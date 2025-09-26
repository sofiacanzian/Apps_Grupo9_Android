// Archivo: PasswordResetRequest.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(
    val email: String,
    val newPassword: String,
    // ✅ CAMBIO: De 'otpCode' a 'otp' para coincidir con el backend
    val otp: String
)