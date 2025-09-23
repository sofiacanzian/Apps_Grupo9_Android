// Archivo: User.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable
import java.util.Date
import kotlinx.serialization.Contextual

@Serializable
data class User(
    val id: String,
    val name: String? = null,
    val email: String?, // <-- ¡CORREGIDO! Ahora puede ser nulo
    val lastName: String? = null,
    val memberId: String? = null,
    @Contextual
    val birthDate: Date? = null,
    val profilePhotoUrl: String? = null
)