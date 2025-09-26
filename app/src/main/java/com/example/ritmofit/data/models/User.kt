package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable
import java.util.Date
import kotlinx.serialization.Contextual

@Serializable
data class User(
    val id: String,
    val name: String? = null,
    val email: String?,
    val lastName: String? = null,
    val memberId: String? = null,

    // CORRECCIÓN: Añadidos para coincidir con la respuesta del servidor (Logcat).
    val phoneNumber: String? = null,
    val address: String? = null,

    @Contextual
    val birthDate: Date? = null,
    val profilePhotoUrl: String? = null
)