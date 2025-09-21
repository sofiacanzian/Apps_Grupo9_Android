// Archivo: ProfileViewModel.kt (Sin cambios, ya que está bien)
package com.example.ritmofit.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ritmofit.data.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var user by mutableStateOf(getMockUser())
        private set

    var isEditing by mutableStateOf(false)
        private set

    var username by mutableStateOf(user.username)
        private set

    var email by mutableStateOf(user.email)
        private set

    fun onUsernameChange(newUsername: String) {
        username = newUsername
    }

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun toggleEditMode() {
        isEditing = !isEditing
    }

    fun saveProfile() {
        viewModelScope.launch {
            delay(1000) // Simula la espera de la red
            user = user.copy(
                username = username,
                email = email
            )
            isEditing = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            delay(500) // Simula el cierre de sesión
            // Lógica para navegar de regreso a la pantalla de login
        }
    }
}

fun getMockUser(): User {
    return User(
        id = "1",
        username = "horacio.uade",
        email = "horacio@uade.edu.ar"
    )
}