package com.example.ritmofit.ui.classes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    classId: String,
    viewModel: ClassViewModel,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la Clase") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        val classes = (viewModel.uiState.value as? ClassesUiState.Success)?.classes
        val class_ = classes?.find { it.id == classId }

        class_?.let { selectedClass ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = selectedClass.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Información principal con íconos
                DetailItemWithIcon(
                    icon = Icons.Default.Person,
                    label = "Profesor",
                    value = selectedClass.instructor
                )
                DetailItemWithIcon(
                    icon = Icons.Default.Schedule,
                    label = "Duración",
                    value = "${selectedClass.duration} minutos"
                )
                DetailItemWithIcon(
                    icon = Icons.Default.LocationOn,
                    label = "Ubicación",
                    value = selectedClass.location
                )
                DetailItemWithIcon(
                    icon = Icons.Default.Category,
                    label = "Disciplina",
                    value = selectedClass.discipline
                )

                // Cupos disponibles con barra de progreso
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Cupos disponibles",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "${selectedClass.availableSpots}/${selectedClass.totalSpots}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    LinearProgressIndicator(
                        progress = selectedClass.availableSpots.toFloat() / selectedClass.totalSpots,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                DetailItemWithIcon(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha y hora",
                    value = selectedClass.datetime
                )
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Clase no encontrada")
            }
        }
    }
}

@Composable
private fun DetailItemWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
