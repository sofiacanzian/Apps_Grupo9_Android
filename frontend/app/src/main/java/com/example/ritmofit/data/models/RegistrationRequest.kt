// Archivo: RegistrationRequest.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequest(
    val email: String,
    val password: String
)