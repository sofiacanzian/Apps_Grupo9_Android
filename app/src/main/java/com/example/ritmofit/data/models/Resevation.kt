package com.example.ritmofit.data.models

/**
 * Modelo para las reservas de clases
 */
data class Reservation(
    val id: String = "",
    val userId: String = "",
    val gymClass: GymClass = GymClass(),
    val status: ReservationStatus = ReservationStatus.CONFIRMED,
    val reservedAt: Long = System.currentTimeMillis(),
    val checkedInAt: Long? = null,
    val qrCode: String = "", // código QR para check-in
    val rating: ClassRating? = null
)

/**
 * Estados de la reserva
 */
enum class ReservationStatus(val displayName: String) {
    CONFIRMED("Confirmada"),
    CANCELLED("Cancelada"),
    EXPIRED("Expirada"),
    COMPLETED("Completada"),
    CHECKED_IN("Check-in realizado")
}

/**
 * Calificación de la clase
 */
data class ClassRating(
    val stars: Int = 0, // 1-5
    val comment: String = "",
    val ratedAt: Long = System.currentTimeMillis()
)

/**
 * Request para crear una reserva
 */
data class CreateReservationRequest(
    val classId: String,
    val userId: String
)

/**
 * Request para cancelar una reserva
 */
data class CancelReservationRequest(
    val reservationId: String,
    val userId: String
)

/**
 * Request para check-in con QR
 */
data class CheckInRequest(
    val qrCode: String,
    val userId: String
)

/**
 * Historial de asistencias
 */
data class AttendanceHistory(
    val reservations: List<Reservation> = emptyList(),
    val totalClasses: Int = 0,
    val thisMonthClasses: Int = 0
)

/**
 * Estados de las operaciones con reservas
 */
sealed class ReservationState {
    object Idle : ReservationState()
    object Loading : ReservationState()
    data class Success(val message: String) : ReservationState()
    data class Error(val message: String) : ReservationState()
}