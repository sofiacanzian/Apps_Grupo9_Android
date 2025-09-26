// Archivo: ClassesViewModel.kt (Final - Listo para usar)
package com.example.ritmofit.ui.theme.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ritmofit.RitmoFitApplication
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.network.ApiService
import com.example.ritmofit.network.FilterResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

// Data Class para encapsular los filtros seleccionados
data class FilterCriteria(
    val location: String? = null,
    val discipline: String? = null,
    val date: String? = null // Formato: YYYY-MM-DD
)

class ClassesViewModel(
    private val apiService: ApiService
) : ViewModel() {

    // ----------------------------------------------------
    // ESTADOS UI
    // ----------------------------------------------------

    sealed class ClassesUiState {
        object Loading : ClassesUiState()
        data class Success(val classes: List<GymClass>) : ClassesUiState()
        data class Error(val message: String) : ClassesUiState()
    }

    private val _classesState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val classesState: StateFlow<ClassesUiState> = _classesState.asStateFlow()

    private val _filterOptions = MutableStateFlow<FilterResponse?>(null)
    val filterOptions: StateFlow<FilterResponse?> = _filterOptions.asStateFlow()

    // Estado para los filtros seleccionados por el usuario
    private val _currentFilters = MutableStateFlow(FilterCriteria())
    val currentFilters: StateFlow<FilterCriteria> = _currentFilters.asStateFlow()

    // ----------------------------------------------------
    // INICIALIZACIÓN Y REACTIVIDAD
    // ----------------------------------------------------

    init {
        // Carga las opciones de filtro (no bloqueante)
        fetchFilterOptions()

        // 💡 LÓGICA REACTIVA CLAVE: Cada vez que _currentFilters cambia (por setFilter o clearFilters),
        // se dispara la recarga de clases con los nuevos parámetros.
        viewModelScope.launch {
            _currentFilters.collect { filters ->
                fetchClasses(filters)
            }
        }
    }

    // ----------------------------------------------------
    // FUNCIONES DE FILTRO
    // ----------------------------------------------------

    private fun fetchFilterOptions() {
        viewModelScope.launch {
            try {
                val response = apiService.getFilters()
                if (response.isSuccessful) {
                    _filterOptions.value = response.body()
                }
            } catch (e: Exception) {
                // Error al obtener opciones de filtro, no bloquea la UI principal
            }
        }
    }

    /**
     * Establece el nuevo valor del filtro. Los filtros nulos se mantienen.
     * Esto dispara automáticamente la recarga de clases gracias al collect en init.
     */
    fun setFilter(location: String? = null, discipline: String? = null, date: String? = null) {
        _currentFilters.update { current ->
            // Si el valor pasado no es nulo, actualiza el filtro; si es nulo, mantiene el valor actual.
            current.copy(
                location = location ?: current.location,
                discipline = discipline ?: current.discipline,
                date = date ?: current.date
            )
        }
    }

    /**
     * Limpia los filtros (establece a FilterCriteria() por defecto) y recarga las clases.
     */
    fun clearFilters() {
        _currentFilters.value = FilterCriteria()
    }

    // ----------------------------------------------------
    // FUNCIÓN DE CARGA DE CLASES (Ahora acepta filtros)
    // ----------------------------------------------------

    fun fetchClasses(filters: FilterCriteria) {
        viewModelScope.launch {
            _classesState.value = ClassesUiState.Loading
            try {
                // Llama a la API enviando los filtros como Query Parameters
                val response = apiService.getClasses(
                    location = filters.location,
                    discipline = filters.discipline,
                    date = filters.date
                )
                if (response.isSuccessful) {
                    val classes = response.body() ?: emptyList()
                    _classesState.value = ClassesUiState.Success(classes)
                } else {
                    _classesState.value = ClassesUiState.Error("Error al cargar las clases: ${response.code()}. Verifique la API.")
                }
            } catch (e: IOException) {
                _classesState.value = ClassesUiState.Error("Error de red. Verifique su conexión.")
            } catch (e: Exception) {
                _classesState.value = ClassesUiState.Error("Error inesperado: ${e.message}. El modelo de datos de clase podría no coincidir con la respuesta del servidor.")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                if (modelClass.isAssignableFrom(ClassesViewModel::class.java)) {
                    // Asegúrate de que RitmoFitApplication.container.apiService devuelve tu implementación de ApiService
                    return ClassesViewModel(
                        (application as RitmoFitApplication).container.apiService
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}