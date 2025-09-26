package com.example.ritmofit.ui.classes

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Importaciones actualizadas/correctas
import com.example.ritmofit.data.models.GymClass 
import com.example.ritmofit.data.models.ClassFilters 
import com.example.ritmofit.network.ClassService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 10

sealed class ClassesUiState {
    object Loading : ClassesUiState()
    data class Success(val classes: List<GymClass>) : ClassesUiState() // Cambiado a GymClass
    data class Error(val message: String) : ClassesUiState()
}

class ClassViewModel(private val classService: ClassService) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // _classes ahora es una lista de GymClass
    private val _classes = mutableStateListOf<GymClass>() 
    // val classes ya no es necesaria si _uiState.Success(classes) se usa directamente
    // o se puede mantener si se necesita la lista completa por separado de la UI Paginada.
    // Por simplicidad, la comentaré por ahora, ya que la UI parece consumir desde uiState.Success
    // val classes: List<GymClass> get() = _classes.toList()

    var currentFilters by mutableStateOf(ClassFilters()) // Usa la nueva ClassFilters
        private set

    var currentPage by mutableStateOf(1)
        private set

    var hasMorePages by mutableStateOf(true)
        private set

    init {
        loadClasses()
    }

    fun loadClasses(resetList: Boolean = false) {
        if (resetList) {
            currentPage = 1
            _classes.clear() // Limpia la lista de GymClass
        }

        // Evitar múltiples cargas si ya está cargando Y no es un reset
        if (_uiState.value is ClassesUiState.Loading && !resetList) return

        viewModelScope.launch {
            _uiState.value = ClassesUiState.Loading 
            try {
                val response = classService.getClasses(
                    sede = currentFilters.sede,
                    discipline = currentFilters.discipline,
                    date = currentFilters.date,
                    page = currentPage,
                    size = PAGE_SIZE
                )

                if (response.isSuccessful) {
                    val newClasses = response.body() ?: emptyList() // newClasses es List<GymClass>
                    hasMorePages = newClasses.size >= PAGE_SIZE
                    
                    // Si es la primera página o un reset, reemplazar. Si no, añadir.
                    if (currentPage == 1) {
                        _classes.clear()
                    }
                    _classes.addAll(newClasses)
                    
                    // Actualizar el estado de la UI con la lista completa actual
                    _uiState.value = ClassesUiState.Success(_classes.toList())
                } else {
                    _uiState.value = ClassesUiState.Error("Error al cargar las clases: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = ClassesUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun updateFilters(filters: ClassFilters) { // Usa la nueva ClassFilters
        currentFilters = filters
        loadClasses(resetList = true)
    }

    fun loadNextPage() {
        if (hasMorePages && _uiState.value !is ClassesUiState.Loading) {
            currentPage++
            loadClasses()
        }
    }
}
