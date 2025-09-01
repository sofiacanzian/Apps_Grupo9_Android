package com.example.ritmofit.ui.theme.profile

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

    var name by mutableStateOf(user.name)
        private set

    var email by mutableStateOf(user.email)
        private set

    var birthDate by mutableStateOf(user.birthDate ?: "")
        private set

    var age by mutableStateOf(user.age?.toString() ?: "")
        private set

    var gender by mutableStateOf(user.gender ?: "")
        private set

    var height by mutableStateOf(user.height?.toString() ?: "")
        private set

    var weight by mutableStateOf(user.weight?.toString() ?: "")
        private set

    var partnerNumber by mutableStateOf(user.partnerNumber ?: "")
        private set

    fun onNameChange(newName: String) {
        name = newName
    }

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onBirthDateChange(newDate: String) {
        birthDate = newDate
    }

    fun onAgeChange(newAge: String) {
        age = newAge
    }

    fun onGenderChange(newGender: String) {
        gender = newGender
    }

    fun onHeightChange(newHeight: String) {
        height = newHeight
    }

    fun onWeightChange(newWeight: String) {
        weight = newWeight
    }

    fun onPartnerNumberChange(newNumber: String) {
        partnerNumber = newNumber
    }

    fun toggleEditMode() {
        isEditing = !isEditing
    }

    fun saveProfile() {
        viewModelScope.launch {
            delay(1000) // Simula la espera de la red
            user = user.copy(
                name = name,
                email = email,
                birthDate = birthDate,
                age = age.toIntOrNull(),
                gender = gender,
                height = height.toDoubleOrNull(),
                weight = weight.toDoubleOrNull(),
                partnerNumber = partnerNumber
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
        name = "Horacio",
        email = "horacio@uade.edu.ar",
        birthDate = "15/05/1990",
        age = 34,
        gender = "Masculino",
        height = 1.75,
        weight = 75.0,
        partnerNumber = "12345"
    )
}