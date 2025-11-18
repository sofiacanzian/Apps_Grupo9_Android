package com.example.ritmofit.ui.theme.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.FitnessGoal
import com.example.ritmofit.data.models.GoalPeriodType
import com.example.ritmofit.data.models.GoalStatus
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.ritmofit.ui.theme.goals.GoalsViewModel.GoalFormState

@Composable
fun GoalsScreen(
    goalsViewModel: GoalsViewModel = viewModel(factory = GoalsViewModel.Factory),
    paddingValues: PaddingValues = PaddingValues()
) {
    val goalsState by goalsViewModel.goalsState.collectAsState()
    val isFormVisible by goalsViewModel.isFormVisible.collectAsState()
    val formState by goalsViewModel.formState.collectAsState()
    val disciplines by goalsViewModel.disciplineOptions.collectAsState()
    val isProcessing by goalsViewModel.isProcessing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var goalPendingDeletion by remember { mutableStateOf<FitnessGoal?>(null) }

    LaunchedEffect(Unit) {
        goalsViewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message = message)
        }
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { goalsViewModel.openCreateGoalForm() }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo objetivo")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Objetivos y seguimiento",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Crea metas semanales o mensuales y recibe alertas cuando estés cerca de cumplirlas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            when (val state = goalsState) {
                GoalsViewModel.GoalsUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is GoalsViewModel.GoalsUiState.Error -> GoalsErrorState(
                    message = state.message,
                    onRetry = goalsViewModel::refreshGoals
                )

                is GoalsViewModel.GoalsUiState.Success -> {
                    if (state.goals.isEmpty()) {
                        GoalsEmptyState(onCreateGoal = goalsViewModel::openCreateGoalForm)
                    } else {
                        GoalsList(
                            goals = state.goals,
                            onEdit = goalsViewModel::openEditGoalForm,
                            onDelete = { goalPendingDeletion = it }
                        )
                    }
                }
            }
        }
    }

    if (isFormVisible) {
        GoalFormDialog(
            formState = formState,
            disciplines = disciplines,
            isProcessing = isProcessing,
            onDismiss = goalsViewModel::closeForm,
            onTitleChange = goalsViewModel::onTitleChange,
            onTargetChange = goalsViewModel::onTargetCountChange,
            onPeriodChange = goalsViewModel::onPeriodChange,
            onDisciplineChange = goalsViewModel::onDisciplineChange,
            onConfirm = goalsViewModel::submitGoal
        )
    }

    goalPendingDeletion?.let { goal ->
        ConfirmDeleteGoalDialog(
            goalTitle = goal.title,
            isProcessing = isProcessing,
            onDismiss = { goalPendingDeletion = null },
            onConfirm = {
                goalsViewModel.deleteGoal(goal.id)
                goalPendingDeletion = null
            }
        )
    }
}

@Composable
private fun GoalsList(
    goals: List<FitnessGoal>,
    onEdit: (FitnessGoal) -> Unit,
    onDelete: (FitnessGoal) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(goals, key = { it.id }) { goal ->
            GoalCard(goal = goal, onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@Composable
private fun GoalCard(
    goal: FitnessGoal,
    onEdit: (FitnessGoal) -> Unit,
    onDelete: (FitnessGoal) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = goal.periodLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                GoalStatusBadge(status = goal.status)
                IconButton(onClick = { onEdit(goal) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar objetivo")
                }
                IconButton(onClick = { onDelete(goal) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar objetivo")
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { goal.progressPercentage.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${goal.currentProgress} de ${goal.targetCount} asistencias registradas",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            goal.discipline?.let {
                Text(
                    text = "Disciplina: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = goal.status != GoalStatus.IN_PROGRESS) {
                val message = when (goal.status) {
                    GoalStatus.NEAR_COMPLETION -> "¡Estás a ${goal.remaining} clase(s) de lograrlo!"
                    GoalStatus.COMPLETED -> "Objetivo alcanzado en este periodo."
                    else -> ""
                }
                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (goal.status == GoalStatus.COMPLETED) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalStatusBadge(status: GoalStatus) {
    val (label, container, contentColor) = when (status) {
        GoalStatus.COMPLETED -> Triple("Completado", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        GoalStatus.NEAR_COMPLETION -> Triple("Casi logrado", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        GoalStatus.IN_PROGRESS -> Triple("En progreso", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    }
    AssistChip(
        onClick = {},
        label = { Text(label, color = contentColor) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(containerColor = container)
    )
}

@Composable
private fun GoalsEmptyState(onCreateGoal: () -> Unit) {
    Surface(
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Todavía no tienes objetivos.",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Crea un objetivo para empezar a medir tus asistencias y recibir recordatorios.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onCreateGoal) {
                Text("Crear mi primer objetivo")
            }
        }
    }
}

@Composable
private fun GoalsErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun GoalFormDialog(
    formState: GoalFormState,
    disciplines: List<String>,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onPeriodChange: (GoalPeriodType) -> Unit,
    onDisciplineChange: (String?) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Text(if (formState.goalId == null) "Nuevo objetivo" else "Editar objetivo")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = onTitleChange,
                    label = { Text("Nombre del objetivo") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = formState.targetCountInput,
                    onValueChange = onTargetChange,
                    label = { Text("Cantidad de asistencias") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(
                    text = "Periodo",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalPeriodType.values().forEach { period ->
                        FilterChip(
                            selected = formState.periodType == period,
                            onClick = { onPeriodChange(period) },
                            label = { Text(period.toDisplayName()) }
                        )
                    }
                }
                DisciplineDropdown(
                    selectedValue = formState.discipline,
                    options = disciplines,
                    onDisciplineChange = onDisciplineChange
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isProcessing) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (formState.goalId == null) "Guardar" else "Actualizar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisciplineDropdown(
    selectedValue: String?,
    options: List<String>,
    onDisciplineChange: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue ?: "Todas las disciplinas",
            onValueChange = {},
            readOnly = true,
            label = { Text("Disciplina") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = {
                    onDisciplineChange(null)
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onDisciplineChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun ConfirmDeleteGoalDialog(
    goalTitle: String,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = { Text("Eliminar objetivo") },
        text = {
            Text("¿Seguro que deseas eliminar \"$goalTitle\"? Podrás crearlo nuevamente cuando lo necesites.")
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isProcessing) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text("Cancelar")
            }
        }
    )
}

private fun GoalPeriodType.toDisplayName(): String = when (this) {
    GoalPeriodType.WEEK -> "Semanal"
    GoalPeriodType.MONTH -> "Mensual"
}
