// Archivo: Location.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Location(
    // 🚀 CAMBIO CLAVE: Hacemos '_id' opcional (String?) y lo inicializamos a null.
    // Esto resuelve el error "Field '_id' is required but it was missing".
    @SerialName("_id") val id: String? = null,
    val name: String
    // NOTA: Los campos professor y duration se movieron a GymClass.kt
)