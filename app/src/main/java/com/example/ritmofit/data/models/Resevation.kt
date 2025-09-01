package com.example.ritmofit.data.models

data class Reservation(
    val id: String,
    val userId: String,
    val gymClass: GymClass,
    val status: ReservationStatus,
    val checkedInAt: Long? = null
)

enum class ReservationStatus(val displayName: String) {
    CONFIRMED("Confirmada"),
    CANCELLED("Cancelada"),
    COMPLETED("Completada")
}