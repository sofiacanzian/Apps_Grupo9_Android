// Archivo: PasswordResetRequest.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(
    val email: String,
    val newPassword: String,
    val otp: String // ✅ Esto es correcto
)