// Archivo: ProfileViewModel.kt
package com.example.ritmofit.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ritmofit.RitmoFitApplication
import com.example.ritmofit.data.models.TrainingPreferences
import com.example.ritmofit.data.models.User
import com.example.ritmofit.network.ApiService
import com.example.ritmofit.network.FilterResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

class ProfileViewModel(
    private val apiService: ApiService
) : ViewModel() {
    sealed class ProfileUiState {
        object Loading : ProfileUiState()
        data class Success(val user: User) : ProfileUiState()
        data class Error(val message: String) : ProfileUiState()
    }

    sealed class PreferencesUiState {
        object Loading : PreferencesUiState()
        data class Success(val preferences: TrainingPreferences) : PreferencesUiState()
        data class Error(val message: String) : PreferencesUiState()
    }

    sealed class FilterUiState {
        object Loading : FilterUiState()
        data class Success(val filters: FilterResponse) : FilterUiState()
        data class Error(val message: String) : FilterUiState()
    }

    private val _userProfileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val userProfileState: StateFlow<ProfileUiState> = _userProfileState.asStateFlow()

    private val _preferencesState = MutableStateFlow<PreferencesUiState>(PreferencesUiState.Loading)
    val preferencesState: StateFlow<PreferencesUiState> = _preferencesState.asStateFlow()

    private val _filtersState = MutableStateFlow<FilterUiState>(FilterUiState.Loading)
    val filtersState: StateFlow<FilterUiState> = _filtersState.asStateFlow()

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

    fun fetchTrainingPreferences(userId: String) {
        viewModelScope.launch {
            _preferencesState.value = PreferencesUiState.Loading
            try {
                val response = apiService.getTrainingPreferences(userId)
                if (response.isSuccessful) {
                    _preferencesState.value = PreferencesUiState.Success(
                        response.body() ?: TrainingPreferences.EMPTY
                    )
                } else {
                    _preferencesState.value = PreferencesUiState.Error(
                        "Error al cargar preferencias: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _preferencesState.value = PreferencesUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun saveTrainingPreferences(userId: String, preferences: TrainingPreferences) {
        viewModelScope.launch {
            _preferencesState.value = PreferencesUiState.Loading
            try {
                val response = apiService.updateTrainingPreferences(userId, preferences)
                if (response.isSuccessful) {
                    _preferencesState.value = PreferencesUiState.Success(
                        response.body() ?: TrainingPreferences.EMPTY
                    )
                } else {
                    _preferencesState.value = PreferencesUiState.Error(
                        "Error al guardar preferencias: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _preferencesState.value = PreferencesUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun resetTrainingPreferences(userId: String) {
        viewModelScope.launch {
            _preferencesState.value = PreferencesUiState.Loading
            try {
                val response = apiService.resetTrainingPreferences(userId)
                if (response.isSuccessful) {
                    _preferencesState.value = PreferencesUiState.Success(
                        response.body() ?: TrainingPreferences.EMPTY
                    )
                } else {
                    _preferencesState.value = PreferencesUiState.Error(
                        "Error al restablecer preferencias: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _preferencesState.value = PreferencesUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun fetchFilters() {
        viewModelScope.launch {
            _filtersState.value = FilterUiState.Loading
            try {
                val response = apiService.getFilters()
                if (response.isSuccessful) {
                    _filtersState.value = FilterUiState.Success(
                        response.body() ?: FilterResponse(emptyList(), emptyList())
                    )
                } else {
                    _filtersState.value = FilterUiState.Error(
                        "Error al obtener filtros: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _filtersState.value = FilterUiState.Error("Error: ${e.message}")
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
