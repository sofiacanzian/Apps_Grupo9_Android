// Archivo: ClassesViewModel.kt
package com.example.ritmofit.ui.theme.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ritmofit.RitmoFitApplication
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class ClassesViewModel(
    private val apiService: ApiService
) : ViewModel() {
    // ... (El resto de tu código de ViewModel)
    sealed class ClassesUiState {
        object Loading : ClassesUiState()
        data class Success(val classes: List<GymClass>) : ClassesUiState()
        data class Error(val message: String) : ClassesUiState()
    }

    private val _classesState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val classesState: StateFlow<ClassesUiState> = _classesState.asStateFlow()

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

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                if (modelClass.isAssignableFrom(ClassesViewModel::class.java)) {
                    return ClassesViewModel(
                        (application as RitmoFitApplication).container.apiService
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}