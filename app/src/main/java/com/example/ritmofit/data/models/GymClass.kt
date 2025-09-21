// Archivo: GymClass.kt (Corregido)
package com.example.ritmofit.data.models

import com.example.ritmofit.data.models.Location
import com.example.ritmofit.data.models.Schedule

data class GymClass(
    val id: String,
    val name: String,
    val description: String,
    val schedule: Schedule,
    val duration: Int,
    val instructor: String,
    val location: Location,
    val difficulty: String,
    val availableSpots: Int,
    val imageUrl: String,
    val maxCapacity: Int // AÑADIDO: Propiedad 'maxCapacity'
)