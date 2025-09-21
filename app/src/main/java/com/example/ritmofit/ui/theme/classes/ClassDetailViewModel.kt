// Archivo: ClassDetailViewModel.kt (Corregido)
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

class ClassDetailViewModel : ViewModel() {
    private val _classState = MutableStateFlow<GymClass?>(null)
    val classState: StateFlow<GymClass?> = _classState.asStateFlow()

    fun fetchClassDetails(classId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getClassDetails(classId)
                if (response.isSuccessful) {
                    _classState.value = response.body()
                } else {
                    // Manejar error
                }
            } catch (e: Exception) {
                // Manejar excepción de red
            }
        }
    }
}