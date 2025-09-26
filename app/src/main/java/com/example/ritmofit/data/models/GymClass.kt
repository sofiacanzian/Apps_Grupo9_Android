package com.example.ritmofit.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Sub-modelo para el horario de la clase (DEJADO AQUÍ)
@Serializable
data class Schedule(
    val day: String,
    val startTime: String,
    val endTime: String
)

// Sub-modelo para la ubicación (DEJADO AQUÍ)
@Serializable
data class Location(
    @SerialName("_id")
    val id: String? = null,
    val name: String
)

// Modelo principal de la clase
@Serializable
data class GymClass(
    @SerialName("_id")
    val id: String,
    val name: String,
    val discipline: String,
    val maxCapacity: Int,
    val currentCapacity: Int,
    val schedule: Schedule,
    val location: Location,

    val professor: String? = null,
    val duration: Int? = null,

    val classDate: String? = null, // Formato ISO 8601 (YYYY-MM-DD)

    val isReserved: Boolean? = false
)