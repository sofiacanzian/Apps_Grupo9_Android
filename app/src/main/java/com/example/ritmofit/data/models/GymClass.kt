// Archivo: GymClass.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GymClass(
    @SerialName("_id") val id: String,
    val name: String,
    val description: String? = null,
    val maxCapacity: Int,
    val currentCapacity: Int,
    val classDate: String? = null,
    val schedule: Schedule,
    val location: Location,
    // 🚀 CAMPOS AÑADIDOS/MOVIDOS: professor y duration ahora son campos directos de la clase
    val professor: String,
    val duration: Int
)

// NOTA: Recuerda que Schedule y Location se importan desde sus propios archivos .kt