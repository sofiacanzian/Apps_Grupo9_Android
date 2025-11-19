package com.example.ritmofit.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ritmofit.RitmoFitApplication
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.data.models.SessionManager
import com.example.ritmofit.data.models.TrainingPreferences
import com.example.ritmofit.network.ApiService
import com.example.ritmofit.network.FilterResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject // Importación necesaria para mejor manejo de errores
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val apiService: ApiService
) : ViewModel() {
    private val _classesState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val classesState: StateFlow<ClassesUiState> = _classesState.asStateFlow()

    private val _reservationState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val reservationState: StateFlow<ReservationUiState> = _reservationState.asStateFlow()

    private val _preferencesState = MutableStateFlow<PreferencesUiState>(PreferencesUiState.Loading)
    val preferencesState: StateFlow<PreferencesUiState> = _preferencesState.asStateFlow()

    // Estados de los filtros
    var selectedLocation by mutableStateOf<String?>(null)
    var selectedDiscipline by mutableStateOf<String?>(null)
    var selectedDate by mutableStateOf<Date?>(null)

    private val _filtersState = MutableStateFlow<FilterUiState>(FilterUiState.Loading)
    val filtersState: StateFlow<FilterUiState> = _filtersState.asStateFlow()

    private var cachedPreferences: TrainingPreferences = TrainingPreferences.EMPTY
    private var cachedClasses: List<GymClass> = emptyList()

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

    sealed class FilterUiState {
        object Loading : FilterUiState()
        data class Success(val filters: FilterResponse) : FilterUiState()
        data class Error(val message: String) : FilterUiState()
    }

    sealed class PreferencesUiState {
        object Loading : PreferencesUiState()
        data class Success(val preferences: TrainingPreferences) : PreferencesUiState()
        data class Error(val message: String) : PreferencesUiState()
    }

    // Funciones para establecer los filtros y recargar las clases
    fun setLocationFilter(location: String?) {
        selectedLocation = location
        fetchClasses()
    }

    fun setDisciplineFilter(discipline: String?) {
        selectedDiscipline = discipline
        fetchClasses()
    }

    fun setDateFilter(date: Date?) {
        selectedDate = date
        fetchClasses()
    }

    fun clearFilters() {
        selectedLocation = null
        selectedDiscipline = null
        selectedDate = null
        fetchClasses()
    }

    // Función para resetear el estado de la reserva
    fun resetReservationState() {
        _reservationState.value = ReservationUiState.Idle
    }


    fun fetchClasses() {
        viewModelScope.launch {
            _classesState.value = ClassesUiState.Loading
            try {
                // Formato de la fecha para el backend (YYYY-MM-DD)
                val formattedDate = selectedDate?.let {
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    formatter.format(it)
                }

                val response = apiService.getClasses(
                    location = selectedLocation,
                    discipline = selectedDiscipline,
                    date = formattedDate
                )

                if (response.isSuccessful) {
                    cachedClasses = response.body() ?: emptyList()
                    _classesState.value = ClassesUiState.Success(
                        orderClassesByPreferences(cachedClasses)
                    )
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

    fun fetchFilters() {
        viewModelScope.launch {
            _filtersState.value = FilterUiState.Loading
            try {
                val response = apiService.getFilters()
                if (response.isSuccessful) {
                    val filters = response.body() ?: FilterResponse(emptyList(), emptyList())
                    _filtersState.value = FilterUiState.Success(filters)
                } else {
                    _filtersState.value = FilterUiState.Error("Error al cargar los filtros: ${response.code()}")
                }
            } catch (e: IOException) {
                _filtersState.value = FilterUiState.Error("Error de red. Verifique su conexión.")
            } catch (e: Exception) {
                _filtersState.value = FilterUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun fetchTrainingPreferences() {
        val userId = SessionManager.getUserId()
        if (userId == null) {
            _preferencesState.value = PreferencesUiState.Error("Usuario no autenticado.")
            return
        }

        viewModelScope.launch {
            _preferencesState.value = PreferencesUiState.Loading
            try {
                val response = apiService.getTrainingPreferences(userId)
                if (response.isSuccessful) {
                    cachedPreferences = response.body() ?: TrainingPreferences.EMPTY
                    _preferencesState.value = PreferencesUiState.Success(cachedPreferences)
                    if (cachedClasses.isNotEmpty()) {
                        _classesState.value = ClassesUiState.Success(orderClassesByPreferences(cachedClasses))
                    }
                } else {
                    _preferencesState.value = PreferencesUiState.Error("Error al cargar preferencias: ${response.code()}")
                }
            } catch (e: IOException) {
                _preferencesState.value = PreferencesUiState.Error("Error de red al obtener preferencias.")
            } catch (e: Exception) {
                _preferencesState.value = PreferencesUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun createReservation(gymClass: GymClass) {
        viewModelScope.launch {
            _reservationState.value = ReservationUiState.Loading
            try {
                val userId = SessionManager.getUserId() ?: throw IllegalStateException("User not authenticated.")

                val response = apiService.createReservation(
                    mapOf("userId" to userId, "classId" to gymClass._id)
                )

                if (response.isSuccessful) {
                    _reservationState.value = ReservationUiState.Success
                    // CLAVE: Recargar las clases para que el cupo se actualice en la UI
                    fetchClasses()
                } else {
                    // ✅ Lógica correcta para capturar el error 400 (Bad Request)
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    val errorMessage = try {
                        val json = JSONObject(errorBody)
                        json.getString("message")
                    } catch (e: Exception) {
                        // Fallback si el cuerpo no es JSON con "message"
                        "Error al crear la reserva: ${response.code()}"
                    }
                    _reservationState.value = ReservationUiState.Error(errorMessage)
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

    private fun orderClassesByPreferences(classes: List<GymClass>): List<GymClass> {
        if (cachedPreferences.isEmpty()) return classes
        return classes.sortedByDescending { preferenceScore(it, cachedPreferences) }
    }

    private fun preferenceScore(gymClass: GymClass, preferences: TrainingPreferences): Int {
        var score = 0
        if (preferences.favoriteDisciplines.any { it.equals(gymClass.discipline, ignoreCase = true) }) {
            score += 3
        }
        if (preferences.preferredLocations.any { it.equals(gymClass.location.name, ignoreCase = true) }) {
            score += 2
        }
        if (matchesPreferredTime(gymClass.schedule.startTime, preferences)) {
            score += 1
        }
        return score
    }

    private fun matchesPreferredTime(startTime: String?, preferences: TrainingPreferences): Boolean {
        val minutes = parseMinutes(startTime) ?: return false
        return preferences.preferredTimeRanges.any { range ->
            val startMinutes = parseMinutes(range.start) ?: return@any false
            val endMinutes = parseMinutes(range.end) ?: return@any false
            minutes in startMinutes until endMinutes
        }
    }

    private fun parseMinutes(time: String?): Int? {
        if (time.isNullOrBlank()) return null
        val parts = time.split(":")
        if (parts.size != 2) return null
        val hours = parts[0].toIntOrNull() ?: return null
        val minutes = parts[1].toIntOrNull() ?: return null
        return hours * 60 + minutes
    }

}
