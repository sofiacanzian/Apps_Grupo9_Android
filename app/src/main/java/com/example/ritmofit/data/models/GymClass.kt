package com.example.ritmofit.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Sub-modelo para el horario de la clase
@Serializable
data class Schedule(
    val day: String,
    val startTime: String,
    val endTime: String
)

// Sub-modelo para la ubicación
@Serializable
data class Location(
    // El id en el JSON del backend para Location sigue siendo "_id"
    @SerialName("_id")
    val id: String? = null,
    val name: String
)

// Modelo principal de la clase
@Serializable
data class GymClass(
    // 🚨 CORRECCIÓN CLAVE 1: Mapear el campo 'id' del JSON del servidor
    // al campo '_id' del modelo Kotlin, que es lo que espera tu estructura.
    @SerialName("id")
    val _id: String, // Usamos _id internamente para evitar conflictos

    // 🚨 CORRECCIÓN CLAVE 2: Mapear el campo 'name' del JSON del servidor
    // al campo 'className' del modelo Kotlin.
    @SerialName("name")
    val className: String,

    // CRÍTICO: Mantenemos 'discipline' como String? si puede ser nulo en el JSON
    val discipline: String?,

    val description: String? = null,
    val maxCapacity: Int,
    val currentCapacity: Int,
    val schedule: Schedule,
    val location: Location,

    // Estos campos deben ser nulos si no siempre vienen del backend
    val professor: String?,
    val duration: Int?,
    val classDate: String? = null,

    val isReserved: Boolean? = false
)