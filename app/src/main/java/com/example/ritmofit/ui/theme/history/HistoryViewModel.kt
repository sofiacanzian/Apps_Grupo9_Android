// Archivo: HistoryViewModel.kt
package com.example.ritmofit.ui.theme.history

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
// CAMBIAR: Usar org.threeten.bp en lugar de java.time
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class HistoryViewModel(
    private val apiService: ApiService
) : ViewModel() {

    // --- ESTADOS DE FILTRO DE FECHA ---
    val startDate = MutableStateFlow<LocalDate?>(null)
    val endDate = MutableStateFlow<LocalDate?>(null)

    sealed class ReservationsUiState {
        object Loading : ReservationsUiState()
        data class Success(val reservations: List<Reservation>) : ReservationsUiState()
        data class Error(val message: String) : ReservationsUiState()
    }

    private val _reservationsState = MutableStateFlow<ReservationsUiState>(ReservationsUiState.Loading)
    val reservationsState: StateFlow<ReservationsUiState> = _reservationsState.asStateFlow()

    init {
        fetchUserReservations()
    }

    /**
     * Llama a la API para obtener el historial de asistencias, aplicando filtros de fecha si están presentes.
     */
    fun fetchUserReservations(start: LocalDate? = startDate.value, end: LocalDate? = endDate.value) {
        viewModelScope.launch {
            _reservationsState.value = ReservationsUiState.Loading
            try {
                val userId = SessionManager.userId ?: throw IllegalStateException("User not authenticated.")

                // Convertir LocalDate a String en formato ISO (YYYY-MM-DD)
                // Usamos DateTimeFormatter de org.threeten.bp.format
                val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
                val startDateString = start?.format(isoFormatter)
                val endDateString = end?.format(isoFormatter)

                // LLAMADA A LA FUNCIÓN DE HISTORIAL
                val response = apiService.getAttendanceHistory(
                    userId = userId,
                    startDate = startDateString,
                    endDate = endDateString
                )

                if (response.isSuccessful) {
                    val reservations = response.body() ?: emptyList()
                    _reservationsState.value = ReservationsUiState.Success(reservations)
                } else {
                    _reservationsState.value = ReservationsUiState.Error("Error al cargar el historial: ${response.code()}")
                }
            } catch (e: IOException) {
                _reservationsState.value = ReservationsUiState.Error("Error de red. Verifique su conexión.")
            } catch (e: Exception) {
                _reservationsState.value = ReservationsUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    /** Aplica el filtro llamando a fetchUserReservations con las fechas almacenadas. */
    fun applyDateFilter() {
        fetchUserReservations()
    }

    /** Reinicia el filtro y recarga el historial completo. */
    fun clearFilter() {
        startDate.value = null
        endDate.value = null
        fetchUserReservations(null, null)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                    return HistoryViewModel(
                        (application as RitmoFitApplication).container.apiService
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}