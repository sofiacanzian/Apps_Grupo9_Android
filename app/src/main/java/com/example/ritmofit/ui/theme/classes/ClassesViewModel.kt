// Archivo: ClassesViewModel.kt (Corregido)
package com.example.ritmofit.ui.theme.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class ClassesViewModel : ViewModel() {

    private val _classes = MutableStateFlow<List<GymClass>>(emptyList())
    val classes: StateFlow<List<GymClass>> = _classes.asStateFlow()

    init {
        fetchClasses()
    }

    private fun fetchClasses() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getClasses()
                if (response.isSuccessful) {
                    _classes.value = response.body() ?: emptyList()
                } else {
                    // Manejar error
                }
            } catch (e: Exception) {
                // Manejar excepción de red
            }
        }
    }
}