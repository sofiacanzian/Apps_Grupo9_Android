// Archivo: ProfileViewModel.kt
package com.example.ritmofit.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ritmofit.RitmoFitApplication
import com.example.ritmofit.data.models.User
import com.example.ritmofit.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class ProfileViewModel(
    private val apiService: ApiService
) : ViewModel() {
    // ... (El resto de tu código de ViewModel)
    sealed class ProfileUiState {
        object Loading : ProfileUiState()
        data class Success(val user: User) : ProfileUiState()
        data class Error(val message: String) : ProfileUiState()
    }

    private val _userProfileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val userProfileState: StateFlow<ProfileUiState> = _userProfileState

    fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            _userProfileState.value = ProfileUiState.Loading
            try {
                val response = apiService.getUserProfile(userId)
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        _userProfileState.value = ProfileUiState.Success(user)
                    } else {
                        _userProfileState.value = ProfileUiState.Error("Respuesta de usuario nula")
                    }
                } else {
                    _userProfileState.value = ProfileUiState.Error("Error al cargar el perfil: ${response.code()}")
                }
            } catch (e: Exception) {
                _userProfileState.value = ProfileUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun updateUserProfile(userId: String, user: User) {
        viewModelScope.launch {
            _userProfileState.value = ProfileUiState.Loading
            try {
                val response = apiService.updateUserProfile(userId, user)
                if (response.isSuccessful) {
                    val updatedUser = response.body()
                    if (updatedUser != null) {
                        _userProfileState.value = ProfileUiState.Success(updatedUser)
                    } else {
                        _userProfileState.value = ProfileUiState.Error("Respuesta de usuario nula")
                    }
                } else {
                    _userProfileState.value = ProfileUiState.Error("Error al actualizar el perfil: ${response.code()}")
                }
            } catch (e: Exception) {
                _userProfileState.value = ProfileUiState.Error("Error: ${e.message}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    return ProfileViewModel(
                        (application as RitmoFitApplication).container.apiService
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}