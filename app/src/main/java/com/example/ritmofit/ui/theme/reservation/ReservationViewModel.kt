package com.example.ritmofit.ui.theme.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ritmofit.RitmoFitApplication
import com.example.ritmofit.data.models.Reservation
import com.example.ritmofit.data.models.SessionManager
import com.example.ritmofit.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class ReservationsViewModel(
    private val apiService: ApiService
) : ViewModel() {
    sealed class ReservationsUiState {
        object Loading : ReservationsUiState()
        data class Success(val reservations: List<Reservation>) : ReservationsUiState()
        data class Error(val message: String) : ReservationsUiState()
    }

    private val _reservationsState = MutableStateFlow<ReservationsUiState>(ReservationsUiState.Loading)
    val reservationsState: StateFlow<ReservationsUiState> = _reservationsState.asStateFlow()

    private val _isBooking = MutableStateFlow(false)
    val isBooking: StateFlow<Boolean> = _isBooking.asStateFlow()

    // Ejecutamos la carga inicial de reservas
    init {
        fetchUserReservations()
    }

    fun fetchUserReservations() {
        viewModelScope.launch {
            _reservationsState.value = ReservationsUiState.Loading
            try {
                // CORRECCIÓN CLAVE 1: Usar la función suspend getUserId()
                val userId = SessionManager.getUserId() ?: throw IllegalStateException("User not authenticated.")

                // CORRECCIÓN: La función fue renombrada a 'getReservations' en ApiService.kt
                val response = apiService.getReservations(userId)

                if (response.isSuccessful) {
                    val reservations = response.body() ?: emptyList()
                    _reservationsState.value = ReservationsUiState.Success(reservations)
                } else {
                    _reservationsState.value = ReservationsUiState.Error("Error al cargar las reservas: ${response.code()}")
                }
            } catch (e: IOException) {
                _reservationsState.value = ReservationsUiState.Error("Error de red: ${e.message}")
            } catch (e: IllegalStateException) {
                _reservationsState.value = ReservationsUiState.Error("Error de autenticación: ${e.message}")
            } catch (e: Exception) {
                _reservationsState.value = ReservationsUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun createReservation(classId: String) {
        viewModelScope.launch {
            _isBooking.value = true
            try {
                // CORRECCIÓN CLAVE 2: Usar la función suspend getUserId()
                val userId = SessionManager.getUserId() ?: throw IllegalStateException("User not authenticated.")

                val response = apiService.createReservation(
                    mapOf("userId" to userId, "gymClassId" to classId)
                )

                if (response.isSuccessful) {
                    // Volvemos a llamar a fetchUserReservations() para actualizar la lista de reservas
                    fetchUserReservations()
                } else {
                    // Manejo de error de la API (podrías usar un StateFlow de error separado si es necesario)
                    // Por ahora, solo se maneja en el catch general
                    throw Exception("Fallo en la reserva: Código ${response.code()}")
                }
            } catch (e: IllegalStateException) {
                // Manejo de error de autenticación
            } catch (e: Exception) {
                // Handle network or other errors (se podría notificar al usuario)
            } finally {
                _isBooking.value = false
            }
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            _reservationsState.value = ReservationsUiState.Loading
            try {
                val response = apiService.cancelReservation(reservationId)
                if (response.isSuccessful) {
                    fetchUserReservations()
                } else {
                    _reservationsState.value = ReservationsUiState.Error("Error al cancelar la reserva: ${response.code()}")
                }
            } catch (e: IOException) {
                _reservationsState.value = ReservationsUiState.Error("Error de red: ${e.message}")
            } catch (e: Exception) {
                _reservationsState.value = ReservationsUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                if (modelClass.isAssignableFrom(ReservationsViewModel::class.java)) {
                    return ReservationsViewModel(
                        (application as RitmoFitApplication).container.apiService
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}