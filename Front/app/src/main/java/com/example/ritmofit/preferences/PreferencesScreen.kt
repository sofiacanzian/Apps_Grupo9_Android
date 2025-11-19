package com.example.ritmofit.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.PreferredTimeRange
import com.example.ritmofit.data.models.SessionManager
import com.example.ritmofit.data.models.TrainingPreferences
import com.example.ritmofit.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    profileViewModel: ProfileViewModel = viewModel()
) {
    var currentUserId by remember { mutableStateOf<String?>(null) }
    var isUserIdResolved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        currentUserId = SessionManager.getUserId()
        profileViewModel.fetchFilters()
        isUserIdResolved = true
    }

    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            profileViewModel.fetchTrainingPreferences(userId)
        }
    }

    val preferencesState by profileViewModel.preferencesState.collectAsState()
    val filtersState by profileViewModel.filtersState.collectAsState()
    val scrollState = rememberScrollState()

    if (!isUserIdResolved) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    val resolvedUserId = currentUserId
    if (resolvedUserId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("Acceso denegado. Usuario no autenticado.") }
        return
    }

    PreferencesContent(
        userId = resolvedUserId,
        profileViewModel = profileViewModel,
        preferencesState = preferencesState,
        filtersState = filtersState,
        scrollState = scrollState
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PreferencesContent(
    userId: String,
    profileViewModel: ProfileViewModel,
    preferencesState: ProfileViewModel.PreferencesUiState,
    filtersState: ProfileViewModel.FilterUiState,
    scrollState: androidx.compose.foundation.ScrollState
) {
    val basePreferences = when (preferencesState) {
        is ProfileViewModel.PreferencesUiState.Success -> preferencesState.preferences
        is ProfileViewModel.PreferencesUiState.Error -> TrainingPreferences.EMPTY
        ProfileViewModel.PreferencesUiState.Loading -> null
    }

    var selectedDisciplines by remember(basePreferences) {
        mutableStateOf(basePreferences?.favoriteDisciplines?.toSet() ?: emptySet())
    }
    var selectedLocations by remember(basePreferences) {
        mutableStateOf(basePreferences?.preferredLocations?.toSet() ?: emptySet())
    }
    var selectedTimeRanges by remember(basePreferences) {
        mutableStateOf(basePreferences?.preferredTimeRanges?.toSet() ?: emptySet())
    }

    val availableTimeSlots = remember(basePreferences) {
        val defaults = DefaultTimeRanges.toMutableList()
        basePreferences?.preferredTimeRanges?.forEach { range ->
            if (defaults.none { it.start == range.start && it.end == range.end }) {
                defaults.add(range)
            }
        }
        defaults.toList()
    }

    val isPreferencesLoading = preferencesState is ProfileViewModel.PreferencesUiState.Loading
    val availableDisciplines = when (filtersState) {
        is ProfileViewModel.FilterUiState.Success -> filtersState.filters.disciplines
        else -> emptyList()
    }
    val availableLocations = when (filtersState) {
        is ProfileViewModel.FilterUiState.Success -> filtersState.filters.locations
        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Preferencias de entrenamiento",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Contanos qué disciplinas, sedes y horarios te interesan para ordenar tu Home automáticamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (preferencesState is ProfileViewModel.PreferencesUiState.Error) {
            Text(
                text = preferencesState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (filtersState is ProfileViewModel.FilterUiState.Error) {
            Text(
                text = filtersState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                MultiSelectChipGroup(
                    title = "Disciplinas favoritas",
                    options = availableDisciplines,
                    selectedItems = selectedDisciplines,
                    enabled = !isPreferencesLoading
                ) { value ->
                    selectedDisciplines = if (selectedDisciplines.contains(value)) {
                        selectedDisciplines - value
                    } else {
                        selectedDisciplines + value
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                MultiSelectChipGroup(
                    title = "Sedes frecuentes",
                    options = availableLocations,
                    selectedItems = selectedLocations,
                    enabled = !isPreferencesLoading
                ) { value ->
                    selectedLocations = if (selectedLocations.contains(value)) {
                        selectedLocations - value
                    } else {
                        selectedLocations + value
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TimeRangeChipGroup(
                    title = "Horarios habituales",
                    options = availableTimeSlots,
                    selectedItems = selectedTimeRanges,
                    enabled = !isPreferencesLoading
                ) { range ->
                    selectedTimeRanges = if (selectedTimeRanges.contains(range)) {
                        selectedTimeRanges - range
                    } else {
                        selectedTimeRanges + range
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val preferences = TrainingPreferences(
                    favoriteDisciplines = selectedDisciplines.toList(),
                    preferredLocations = selectedLocations.toList(),
                    preferredTimeRanges = selectedTimeRanges.toList()
                )
                profileViewModel.saveTrainingPreferences(userId, preferences)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isPreferencesLoading
        ) {
            Text("Guardar preferencias")
        }

        TextButton(
            onClick = { profileViewModel.resetTrainingPreferences(userId) },
            enabled = !isPreferencesLoading
        ) {
            Text("Restablecer preferencias")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiSelectChipGroup(
    title: String,
    options: List<String>,
    selectedItems: Set<String>,
    enabled: Boolean,
    onSelectionChange: (String) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    if (options.isEmpty()) {
        Text(
            text = "No hay opciones disponibles.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selectedItems.contains(option),
                onClick = { onSelectionChange(option) },
                enabled = enabled,
                label = { Text(option) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeRangeChipGroup(
    title: String,
    options: List<PreferredTimeRange>,
    selectedItems: Set<PreferredTimeRange>,
    enabled: Boolean,
    onSelectionChange: (PreferredTimeRange) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedItems.any { it.start == option.start && it.end == option.end }
            FilterChip(
                selected = isSelected,
                onClick = { onSelectionChange(option) },
                enabled = enabled,
                label = { Text(option.label) }
            )
        }
    }
}

private val DefaultTimeRanges = listOf(
    PreferredTimeRange("Mañana (06-11 h)", "06:00", "11:00"),
    PreferredTimeRange("Mediodía (11-14 h)", "11:00", "14:00"),
    PreferredTimeRange("Tarde (14-18 h)", "14:00", "18:00"),
    PreferredTimeRange("Noche (18-22 h)", "18:00", "22:00")
)
