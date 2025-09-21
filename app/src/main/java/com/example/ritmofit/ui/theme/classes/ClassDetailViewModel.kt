// Archivo: ClassDetailViewModel.kt
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
import kotlinx.coroutines.launch
import java.io.IOException

class ClassDetailViewModel(private val apiService: ApiService) : ViewModel() {
    // ... (El resto de tu código de ViewModel)
    private val _classState = MutableStateFlow<GymClass?>(null)
    val classState: StateFlow<GymClass?> = _classState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchClassDetails(classId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getClassDetails(classId)
                if (response.isSuccessful) {
                    _classState.value = response.body()
                } else {
                    _errorMessage.value = "Error al cargar los detalles de la clase: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Error de red: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                if (modelClass.isAssignableFrom(ClassDetailViewModel::class.java)) {
                    return ClassDetailViewModel(
                        (application as RitmoFitApplication).container.apiService
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}