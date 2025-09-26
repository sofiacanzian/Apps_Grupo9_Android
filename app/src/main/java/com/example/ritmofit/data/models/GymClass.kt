// Archivo: com/example/ritmofit/data/models/GymClass.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GymClass(
    // ESTO ES CORRECTO: Mapea 'id' del JSON a '_id' en Kotlin,
    // preservando '_id' para compatibilidad con otros archivos.
    @SerialName("id")
    val _id: String,

    // ESTO ES CORRECTO: Mapea 'name' del JSON a 'className' en Kotlin.
    @SerialName("name")
    val className: String,

    val description: String,
    val maxCapacity: Int,
    val currentCapacity: Int,
    val schedule: Schedule,
    val location: Location,
    val discipline: String,
    val professor: String,
    val duration: Int,
    val classDate: String
)

@Serializable
data class Schedule(
    val day: String,
    val startTime: String,
    val endTime: String
)

@Serializable
data class Location(
    val name: String
)

// NOTA: Si tu backend envía "_id" en lugar de "id", usa @SerialName("_id")
// Sin embargo, basándome en tu logcat, usé "id" y "name" para el mapeo.