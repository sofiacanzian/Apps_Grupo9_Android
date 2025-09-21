// Archivo: app/src/main/java/com/example/ritmofit/data/models/GymClass.kt (Corregido)
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GymClass(
    val id: String,
    val className: String,
    val description: String,
    val schedule: Schedule,
    val duration: Int,
    val trainerName: String?,
    val location: Location,
    val difficulty: String,
    val availableSpots: Int,
    val imageUrl: String,
    val maxCapacity: Int,
    val participants: List<String>
)