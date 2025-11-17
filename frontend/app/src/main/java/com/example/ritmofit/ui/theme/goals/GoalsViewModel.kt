package com.example.ritmofit.ui.theme.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.ritmofit.RitmoFitApplication
import com.example.ritmofit.data.models.FitnessGoal
import com.example.ritmofit.data.models.GoalPeriodType
import com.example.ritmofit.data.models.GoalRequest
import com.example.ritmofit.data.models.SessionManager
import com.example.ritmofit.network.ApiService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class GoalsViewModel(
    private val apiService: ApiService
) : ViewModel() {

    sealed class GoalsUiState {
        object Loading : GoalsUiState()
        data class Success(val goals: List<FitnessGoal>) : GoalsUiState()
        data class Error(val message: String) : GoalsUiState()
    }

    private val _goalsState = MutableStateFlow<GoalsUiState>(GoalsUiState.Loading)
    val goalsState: StateFlow<GoalsUiState> = _goalsState.asStateFlow()

    private val _disciplineOptions = MutableStateFlow<List<String>>(emptyList())
    val disciplineOptions: StateFlow<List<String>> = _disciplineOptions.asStateFlow()

    private val _isFormVisible = MutableStateFlow(false)
    val isFormVisible: StateFlow<Boolean> = _isFormVisible.asStateFlow()

    private val _formState = MutableStateFlow(GoalFormState())
    val formState: StateFlow<GoalFormState> = _formState.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    init {
        refreshGoals()
        fetchAvailableDisciplines()
    }

    fun refreshGoals() {
        viewModelScope.launch {
            _goalsState.value = GoalsUiState.Loading
            try {
                val userId = SessionManager.getUserId() ?: throw IllegalStateException("Usuario no autenticado.")
                val response = apiService.getFitnessGoals(userId)
                if (response.isSuccessful) {
                    _goalsState.value = GoalsUiState.Success(response.body().orEmpty())
                } else {
                    _goalsState.value = GoalsUiState.Error("Error al cargar los objetivos: ${response.code()}")
                }
            } catch (e: IOException) {
                _goalsState.value = GoalsUiState.Error("Error de red. Verifica tu conexión.")
            } catch (e: Exception) {
                _goalsState.value = GoalsUiState.Error(e.message ?: "Error inesperado al cargar los objetivos.")
            }
        }
    }

    private fun fetchAvailableDisciplines() {
        viewModelScope.launch {
            try {
                val response = apiService.getFilters()
                if (response.isSuccessful) {
                    _disciplineOptions.value = response.body()?.disciplines ?: emptyList()
                }
            } catch (_: Exception) {
                // Omitimos el error silenciosamente. La creación de objetivos no depende de este listado.
            }
        }
    }

    fun openCreateGoalForm() {
        _formState.value = GoalFormState()
        _isFormVisible.value = true
    }

    fun openEditGoalForm(goal: FitnessGoal) {
        _formState.value = GoalFormState(
            goalId = goal.id,
            title = goal.title,
            targetCountInput = goal.targetCount.toString(),
            periodType = goal.periodType,
            discipline = goal.discipline
        )
        _isFormVisible.value = true
    }

    fun closeForm() {
        _isFormVisible.value = false
    }

    fun onTitleChange(value: String) {
        _formState.update { it.copy(title = value) }
    }

    fun onTargetCountChange(value: String) {
        val sanitized = value.filter { it.isDigit() }.take(3)
        _formState.update { it.copy(targetCountInput = sanitized) }
    }

    fun onPeriodChange(value: GoalPeriodType) {
        _formState.update { it.copy(periodType = value) }
    }

    fun onDisciplineChange(value: String?) {
        _formState.update { it.copy(discipline = value?.takeIf { it.isNotBlank() }) }
    }

    fun submitGoal() {
        val request = buildRequestFromForm() ?: return
        val currentGoalId = _formState.value.goalId
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val isCreating = currentGoalId == null
                val response = if (isCreating) {
                    apiService.createFitnessGoal(request)
                } else {
                    apiService.updateFitnessGoal(currentGoalId!!, request)
                }

                if (response.isSuccessful) {
                    _snackbarMessages.emit(if (isCreating) "Objetivo creado." else "Objetivo actualizado.")
                    _formState.value = GoalFormState()
                    _isFormVisible.value = false
                    refreshGoals()
                } else {
                    _snackbarMessages.emit("No se pudo guardar el objetivo. Código ${response.code()}.")
                }
            } catch (e: IOException) {
                _snackbarMessages.emit("Error de red al guardar el objetivo.")
            } catch (e: Exception) {
                _snackbarMessages.emit(e.message ?: "Error inesperado al guardar el objetivo.")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val response = apiService.deleteFitnessGoal(goalId)
                if (response.isSuccessful) {
                    _snackbarMessages.emit("Objetivo eliminado.")
                    refreshGoals()
                } else {
                    _snackbarMessages.emit("No se pudo eliminar el objetivo. Código ${response.code()}.")
                }
            } catch (e: IOException) {
                _snackbarMessages.emit("Error de red al eliminar el objetivo.")
            } catch (e: Exception) {
                _snackbarMessages.emit(e.message ?: "Error inesperado al eliminar el objetivo.")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun buildRequestFromForm(): GoalRequest? {
        val form = _formState.value
        val target = form.targetCountInput.toIntOrNull()
        if (form.title.isBlank()) {
            viewModelScope.launch { _snackbarMessages.emit("Debes ingresar un nombre para el objetivo.") }
            return null
        }
        if (target == null || target <= 0) {
            viewModelScope.launch { _snackbarMessages.emit("La meta debe ser un número mayor a 0.") }
            return null
        }
        return GoalRequest(
            title = form.title.trim(),
            targetCount = target,
            periodType = form.periodType,
            discipline = form.discipline
        )
    }

    data class GoalFormState(
        val goalId: String? = null,
        val title: String = "",
        val targetCountInput: String = "",
        val periodType: GoalPeriodType = GoalPeriodType.MONTH,
        val discipline: String? = null
    )

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                if (modelClass.isAssignableFrom(GoalsViewModel::class.java)) {
                    return GoalsViewModel(
                        (application as RitmoFitApplication).container.apiService
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
