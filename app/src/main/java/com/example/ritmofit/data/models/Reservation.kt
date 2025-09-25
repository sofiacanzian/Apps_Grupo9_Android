// Archivo: app/src/main/java/com/example/ritmofit/data/models/Reservation.kt
package com.example.ritmofit.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Reservation(
    @SerialName("_id") val id: String,
    // El backend lo retorna populado, pero a veces no, por eso se hace el 'classId: GymClass'
    @SerialName("classId") val classId: GymClass,
    val userId: String,
    val reservationDate: String,
    // ⚠️ CAMBIO CLAVE: Fecha y hora exacta de la clase reservada
    val classDate: String,
    val status: String
)