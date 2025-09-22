// Archivo: User.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable
import java.util.Date // Asegúrate de importar esto
import kotlinx.serialization.Contextual // IMPORTA ESTO

@Serializable
data class User(
    val id: String,
    val name: String? = null,
    val email: String,
    val lastName: String? = null,
    val memberId: String? = null,
    @Contextual // Añade esta anotación
    val birthDate: Date? = null,
    val profilePhotoUrl: String? = null
)