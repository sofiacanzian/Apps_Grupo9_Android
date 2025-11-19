package com.example.ritmofit.profile

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.ritmofit.R
import com.example.ritmofit.data.models.PreferredTimeRange
import com.example.ritmofit.data.models.SessionManager
import com.example.ritmofit.data.models.TrainingPreferences
import com.example.ritmofit.data.models.User
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
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
            profileViewModel.fetchUserProfile(userId)
            profileViewModel.fetchTrainingPreferences(userId)
        }
    }

    val uiState by profileViewModel.userProfileState.collectAsState()
    val preferencesState by profileViewModel.preferencesState.collectAsState()
    val filtersState by profileViewModel.filtersState.collectAsState()
    val scrollState = rememberScrollState()
    var isEditing by remember { mutableStateOf(false) }

    val basePreferences = when (val prefState = preferencesState) {
        is ProfileViewModel.PreferencesUiState.Success -> prefState.preferences
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
    val availableDisciplines = when (val state = filtersState) {
        is ProfileViewModel.FilterUiState.Success -> state.filters.disciplines
        else -> emptyList()
    }
    val availableLocations = when (val state = filtersState) {
        is ProfileViewModel.FilterUiState.Success -> state.filters.locations
        else -> emptyList()
    }

    if (!isUserIdResolved) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (currentUserId == null) {
        Text(
            text = "Acceso denegado. Usuario no autenticado.",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
        return
    }

    val resolvedUserId = currentUserId!!

    when (uiState) {
        is ProfileViewModel.ProfileUiState.Loading -> {
            Text("Cargando perfil...")
        }
        is ProfileViewModel.ProfileUiState.Success -> {
            val user = (uiState as ProfileViewModel.ProfileUiState.Success).user
            // Es importante que estas variables locales usen 'remember' y 'mutableStateOf'
            // para que los campos de texto se puedan editar.
            var name by remember(user.name) { mutableStateOf(user.name ?: "") }
            var lastName by remember(user.lastName) { mutableStateOf(user.lastName ?: "") }
            val memberId = remember { user.memberId ?: "" } // Este campo es readOnly, no necesita ser mutable
            val email = remember { user.email ?: "" } // Este campo es readOnly, no necesita ser mutable

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = remember {
                user.birthDate?.let { dateFormat.format(it) } ?: ""
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (user.profilePhotoUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(user.profilePhotoUrl),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Silueta de persona",
                        modifier = Modifier.size(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    readOnly = !isEditing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Apellido") },
                    readOnly = !isEditing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { /* El email no se edita */ },
                    label = { Text("Email") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = memberId,
                    onValueChange = { /* El número de socio no se edita */ },
                    label = { Text("Número de Socio") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { /* La fecha de nacimiento no se edita aquí */ },
                    label = { Text("Fecha de Nacimiento") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    Button(
                        onClick = {
                            val updatedUser = user.copy(
                                name = name,
                                lastName = lastName
                            )
                            // Usamos el ID cargado previamente
                            profileViewModel.updateUserProfile(resolvedUserId, updatedUser)
                            isEditing = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar cambios")
                    }
                } else {
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar perfil")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Preferencias de entrenamiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Usamos esta informaciA3n para ordenar las clases que ves en la Home.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isPreferencesLoading || filtersState is ProfileViewModel.FilterUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (preferencesState is ProfileViewModel.PreferencesUiState.Error) {
                            Text(
                                text = (preferencesState as ProfileViewModel.PreferencesUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (filtersState is ProfileViewModel.FilterUiState.Error) {
                            Text(
                                text = (filtersState as ProfileViewModel.FilterUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

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
                        profileViewModel.saveTrainingPreferences(resolvedUserId, preferences)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPreferencesLoading
                ) {
                    Text("Guardar preferencias")
                }

                TextButton(
                    onClick = { profileViewModel.resetTrainingPreferences(resolvedUserId) },
                    enabled = !isPreferencesLoading
                ) {
                    Text("Restablecer preferencias")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar SesiA3n")
                }
            }
        }
        is ProfileViewModel.ProfileUiState.Error -> {
            Text("Error al cargar el perfil: ${(uiState as ProfileViewModel.ProfileUiState.Error).message}")
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
            text = "A�n no hay opciones disponibles.",
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
            val isSelected = selectedItems.contains(option)
            FilterChip(
                selected = isSelected,
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
    PreferredTimeRange("MaA�ana (6 a 11 hs)", "06:00", "11:00"),
    PreferredTimeRange("MediodA-a (11 a 14 hs)", "11:00", "14:00"),
    PreferredTimeRange("Tarde (14 a 18 hs)", "14:00", "18:00"),
    PreferredTimeRange("Noche (18 a 22 hs)", "18:00", "22:00")
)
