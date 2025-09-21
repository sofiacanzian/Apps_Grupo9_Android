// Archivo: Reservation.kt
package com.example.ritmofit.data.models

import com.google.gson.annotations.SerializedName

data class Reservation(
    @SerializedName("id")
    val id: String,
    @SerializedName("gymClass")
    val gymClass: GymClass,
    @SerializedName("status")
    val status: ReservationStatus,
    @SerializedName("timestamp")
    val timestamp: String // o un tipo de dato de fecha más específico
)

enum class ReservationStatus(val displayName: String) {
    CONFIRMED("Confirmada"),
    CANCELLED("Cancelada"),
    EXPIRED("Expirada")
}