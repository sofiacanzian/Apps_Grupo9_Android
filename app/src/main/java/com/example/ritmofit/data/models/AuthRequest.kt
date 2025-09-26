// Archivo: app/src/main/java/com/example/ritmofit/data/models/AuthRequest.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    // CRÍTICO: Añadir 'password' como opcional para el flujo de Login
    val password: String? = null,
    // Mantener 'otp' para las peticiones que lo requieran
    val otp: String? = null
)