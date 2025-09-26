package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    // ESTO ES CRÍTICO: Debe ser opcional para que no falle cuando el servidor omite el token.
    val token: String? = null,

    val user: User,
    val message: String? = null
)