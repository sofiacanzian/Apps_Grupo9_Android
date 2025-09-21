// Archivo: HomeViewModel.kt
package com.example.ritmofit.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.network.RetrofitClient
import kotlinx.coroutines.launch
import java.lang.Exception

class HomeViewModel : ViewModel() {

    // Estado que manejará la carga de clases
    val classesState = mutableStateOf(ClassesState())

    init {
        fetchClasses()
    }

    fun fetchClasses() {
        viewModelScope.launch {
            classesState.value = ClassesState(isLoading = true)
            try {
                // La llamada a la API ahora devolverá un objeto Response
                val response = RetrofitClient.apiService.getClasses()

                // Verificamos si la respuesta fue exitosa
                if (response.isSuccessful) {
                    val classes = response.body() ?: emptyList()
                    classesState.value = ClassesState(classes = classes)
                } else {
                    classesState.value = ClassesState(error = "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                classesState.value = ClassesState(error = e.message)
            }
        }
    }
}

// Clase para manejar los estados de la interfaz de usuario
data class ClassesState(
    val classes: List<GymClass> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)