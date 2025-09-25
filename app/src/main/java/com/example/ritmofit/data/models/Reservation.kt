// Archivo: app/src/main/java/com/example/ritmofit/data/models/Reservation.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Reservation(
    @SerialName("_id") val id: String,
    val userId: String,
    val classId: GymClass,
    val reservationDate: String,
    val status: String,
    // --- CAMBIO CLAVE: Fecha real de la clase para el historial y solapamiento ---
    val classDate: String? = null
)