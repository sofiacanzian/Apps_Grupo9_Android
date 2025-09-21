// Archivo: HomeViewModel.kt
package com.example.ritmofit.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ritmofit.RitmoFitApplication
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.data.models.Reservation
import com.example.ritmofit.network.ApiService
import com.example.ritmofit.data.models.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class HomeViewModel(
    private val apiService: ApiService
) : ViewModel() {
    // ... (El resto de tu código de ViewModel)
    private val _classesState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val classesState: StateFlow<ClassesUiState> = _classesState.asStateFlow()

    private val _reservationState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val reservationState: StateFlow<ReservationUiState> = _reservationState.asStateFlow()

    sealed class ClassesUiState {
        object Loading : ClassesUiState()
        data class Success(val classes: List<GymClass>) : ClassesUiState()
        data class Error(val message: String) : ClassesUiState()
    }

    sealed class ReservationUiState {
        object Idle : ReservationUiState()
        object Loading : ReservationUiState()
        object Success : ReservationUiState()
        data class Error(val message: String) : ReservationUiState()
    }

    fun fetchClasses() {
        viewModelScope.launch {
            _classesState.value = ClassesUiState.Loading
            try {
                val response = apiService.getClasses()
                if (response.isSuccessful) {
                    val classes = response.body() ?: emptyList()
                    _classesState.value = ClassesUiState.Success(classes)
                } else {
                    _classesState.value = ClassesUiState.Error("Error al cargar las clases: ${response.code()}")
                }
            } catch (e: IOException) {
                _classesState.value = ClassesUiState.Error("Error de red. Verifique su conexión.")
            } catch (e: Exception) {
                _classesState.value = ClassesUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun createReservation(gymClass: GymClass) {
        viewModelScope.launch {
            _reservationState.value = ReservationUiState.Loading
            try {
                val userId = SessionManager.userId ?: throw IllegalStateException("User not authenticated.")
                val response = apiService.createReservation(
                    mapOf("userId" to userId, "gymClassId" to gymClass.id)
                )

                if (response.isSuccessful) {
                    _reservationState.value = ReservationUiState.Success
                } else {
                    _reservationState.value = ReservationUiState.Error("Error al crear la reserva: ${response.code()}")
                }
            } catch (e: IllegalStateException) {
                _reservationState.value = ReservationUiState.Error(e.message ?: "Error de autenticación.")
            } catch (e: IOException) {
                _reservationState.value = ReservationUiState.Error("Error de red. Verifique su conexión.")
            } catch (e: Exception) {
                _reservationState.value = ReservationUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(
                        (application as RitmoFitApplication).container.apiService
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}