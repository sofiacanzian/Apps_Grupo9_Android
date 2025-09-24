package com.example.ritmofit.ui.classes

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ritmofit.model.Class
import com.example.ritmofit.model.ClassFilters
import com.example.ritmofit.network.ClassService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 10

sealed class ClassesUiState {
    object Loading : ClassesUiState()
    data class Success(val classes: List<Class>) : ClassesUiState()
    data class Error(val message: String) : ClassesUiState()
}

class ClassViewModel(private val classService: ClassService) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _classes = mutableStateListOf<Class>()
    val classes: List<Class> get() = _classes.toList()

    var currentFilters by mutableStateOf(ClassFilters())
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
            _classes.clear()
        }

        if (_uiState.value is ClassesUiState.Loading) return

        viewModelScope.launch {
            try {
                _uiState.value = ClassesUiState.Loading

                val response = classService.getClasses(
                    sede = currentFilters.sede,
                    discipline = currentFilters.discipline,
                    date = currentFilters.date,
                    page = currentPage,
                    size = PAGE_SIZE
                )

                if (response.isSuccessful) {
                    val newClasses = response.body() ?: emptyList()
                    hasMorePages = newClasses.size >= PAGE_SIZE
                    _classes.addAll(newClasses)
                    _uiState.value = ClassesUiState.Success(_classes.toList())
                } else {
                    _uiState.value = ClassesUiState.Error("Error al cargar las clases: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = ClassesUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun updateFilters(filters: ClassFilters) {
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
