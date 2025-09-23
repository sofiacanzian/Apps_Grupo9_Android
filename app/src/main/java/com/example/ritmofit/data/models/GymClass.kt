// Archivo: app/src/main/java/com/example/ritmofit/data/models/GymClass.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GymClass(
    @SerialName("_id") val id: String,
    val name: String,
    val description: String,
    val maxCapacity: Int,
    val currentCapacity: Int, // <-- CORREGIDO: coincide con el servidor
    val schedule: Schedule,
    val location: Location,
    val professor: String?, // <-- CORREGIDO: coincide con el servidor
    val duration: Int?
)