// Archivo: ClassesScreen.kt
package com.example.ritmofit.ui.theme.classes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
// ⚠️ Nota: Asegúrate de que estas dependencias de fecha estén en tu build.gradle
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

// ⚠️ NOTA IMPORTANTE: Debes tener definido ClassesViewModel en tu proyecto.
// Esto es solo un placeholder para que compile:
// interface ClassesViewModel : ViewModel { ... }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    onNavigateBack: () -> Unit,
    onClassClick: (GymClass) -> Unit,
    // ⚠️ Se asume que tienes un ClassesViewModel.Factory definido
    classesViewModel: ClassesViewModel = viewModel()
) {
    val classesState by classesViewModel.classesState.collectAsState()

    // 💡 Ejecutar la carga de datos una sola vez
    LaunchedEffect(Unit) {
        // Asumiendo que esta función existe en tu ClassesViewModel
        classesViewModel.fetchClasses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clases Disponibles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // ⚠️ Se asume que ClassesViewModel.ClassesUiState es una sealed class con Loading, Success, Error
            when (val state = classesState) {
                is ClassesViewModel.ClassesUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ClassesViewModel.ClassesUiState.Success -> {
                    if (state.classes.isEmpty()) {
                        Text(text = "No hay clases disponibles.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.classes) { gymClass ->
                                GymClassItem(gymClass = gymClass, onClassClick = onClassClick)
                            }
                        }
                    }
                }
                is ClassesViewModel.ClassesUiState.Error -> {
                    Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                // Manejar un estado inicial si lo tienes
                else -> { /* Nada */ }
            }
        }
    }
}

@Composable
fun GymClassItem(gymClass: GymClass, onClassClick: (GymClass) -> Unit) {

    // Lógica de Formato de Fecha
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    }

    val classDateFormatted = remember(gymClass.classDate, gymClass.schedule.day) {
        val isoDate = gymClass.classDate
        if (isoDate != null) {
            try {
                // Parseo de fecha (Asume que gymClass.classDate es una fecha ISO: YYYY-MM-DD)
                val date = LocalDate.parse(isoDate)
                date.format(dateFormatter)
            } catch (e: Exception) {
                // Si falla el parseo, muestra el día de la semana
                gymClass.schedule.day.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        } else {
            // Si classDate es nulo, muestra el día de la semana
            gymClass.schedule.day.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClassClick(gymClass) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ✅ CORRECCIÓN/VERIFICACIÓN: Usa discipline si está, sino usa name
            val className = gymClass.discipline ?: gymClass.className

            Text(
                text = className,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Horario: usando la fecha formateada (dd/MM/yyyy)
            Text(
                text = "Horario: ${classDateFormatted}, ${gymClass.schedule.startTime} - ${gymClass.schedule.endTime}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Ubicación: ${gymClass.location.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Cupos: ${gymClass.currentCapacity} / ${gymClass.maxCapacity}",
                style = MaterialTheme.typography.bodyMedium
            )

            // ✅ MANEJO SEGURO DE NULOS
            gymClass.professor?.let { professor ->
                Text(
                    text = "Profesor: $professor",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // ✅ MANEJO SEGURO DE NULOS
            gymClass.duration?.let { duration ->
                Text(
                    text = "Duración: $duration minutos",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}