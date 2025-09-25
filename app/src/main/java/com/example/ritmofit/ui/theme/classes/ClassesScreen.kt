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
import androidx.compose.runtime.remember // ⚠️ NUEVO
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight // ⚠️ NUEVO
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
// ⚠️ ELIMINA el import de GymClassCard si no existe como archivo aparte.
// import com.example.ritmofit.home.GymClassCard

// ⚠️ NECESITAS ESTOS IMPORTS (Asegúrate de que tus dependencias de gradle estén bien configuradas para threetenbp)
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    onNavigateBack: () -> Unit,
    onClassClick: (GymClass) -> Unit,
    classesViewModel: ClassesViewModel = viewModel()
) {
    val classesState by classesViewModel.classesState.collectAsState()

    LaunchedEffect(Unit) {
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
            when (val state = classesState) {
                is ClassesViewModel.ClassesUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is ClassesViewModel.ClassesUiState.Success -> {
                    if (state.classes.isEmpty()) {
                        Text(text = "No hay clases disponibles.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), // Agregué padding para mejor visualización
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.classes) { gymClass ->
                                // 🛑 REEMPLAZO DEL COMPONENTE GymClassCard POR EL CÓDIGO ANIDADO
                                GymClassItem(gymClass = gymClass, onClassClick = onClassClick)
                                // 🛑 FIN REEMPLAZO
                            }
                        }
                    }
                }
                is ClassesViewModel.ClassesUiState.Error -> {
                    Text(text = "Error: ${state.message}")
                }
            }
        }
    }
}

// 🛑 NUEVO COMPOSABLE PARA DIBUJAR LA TARJETA CON LA LÓGICA DE FECHA
@Composable
fun GymClassItem(gymClass: GymClass, onClassClick: (GymClass) -> Unit) {

    // Lógica de Formato de Fecha (sin cambios)
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "ES"))
    }

    val classDateFormatted = remember(gymClass.classDate) {
        val isoDate = gymClass.classDate
        if (isoDate != null) {
            try {
                val date = LocalDate.parse(isoDate)
                date.format(dateFormatter)
            } catch (e: Exception) {
                gymClass.schedule.day.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        } else {
            gymClass.schedule.day.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClassClick(gymClass) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = gymClass.name,
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
            // 🚀 CAMBIO: Ahora se accede directamente a gymClass.professor
            Text(
                text = "Profesor: ${gymClass.professor}",
                style = MaterialTheme.typography.bodyMedium
            )
            // 🚀 CAMBIO: Ahora se accede directamente a gymClass.duration
            Text(
                text = "Duración: ${gymClass.duration} minutos",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}