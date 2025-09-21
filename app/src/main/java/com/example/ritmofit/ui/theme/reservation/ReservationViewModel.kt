// Archivo: ReservationsViewModel.kt (Corregido)
package com.example.ritmofit.ui.theme.reservation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.data.models.Reservation
import com.example.ritmofit.data.models.ReservationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class ReservationsViewModel : ViewModel() {

    // Cambiamos a val para que el estado sea inmutable desde fuera
    var reservations by mutableStateOf(getMockReservations())
        private set

    var isBooking by mutableStateOf(false)
        private set

    fun createReservation(gymClass: GymClass) {
        viewModelScope.launch {
            isBooking = true
            delay(1500) // Simula la llamada a la API
            val newReservation = Reservation(
                id = UUID.randomUUID().toString(),
                gymClass = gymClass,
                status = ReservationStatus.CONFIRMED,
                timestamp = "2023-10-27T10:00:00Z" // Parámetro 'timestamp' añadido
            )
            reservations = reservations + newReservation
            isBooking = false
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            delay(1000) // Simula la llamada a la API para cancelar
            reservations = reservations.filter { it.id != reservationId }
        }
    }
}

private fun getMockReservations(): List<Reservation> {
    // Retornamos una lista vacía para que no haya reservas al inicio,
    // o puedes agregar datos de prueba si lo necesitas para la vista previa.
    return emptyList()
}