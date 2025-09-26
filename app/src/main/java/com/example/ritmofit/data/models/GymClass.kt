package com.example.ritmofit.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GymClass(
    // 🔑 Campo CRÍTICO 1: Mapea 'id' del JSON (el _id de Mongo)
    @SerialName("id")
    val _id: String,

    // Mapea 'name' del JSON a 'className'
    @SerialName("name")
    val className: String,

    // Convertido a nullable para ser robusto
    val description: String?,
    val maxCapacity: Int,
    val currentCapacity: Int,
    val schedule: Schedule,
    val location: Location,

    // 🔑 Campo CRÍTICO 2: La disciplina es obligatoria
    val discipline: String,

    // 💡 Modificados a Nullable (String? / Int?) para aceptar 'null' del Backend
    val professor: String?,
    val duration: Int?,

    // 🔑 Campo CRÍTICO 3: La fecha de la clase (obligatorio para el listado y reservas)
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