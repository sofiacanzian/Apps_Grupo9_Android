package com.example.ritmofit.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.ui.text.font.FontWeight
// ⚠️ Nota: Asegúrate de que estas dependencias de fecha estén en tu build.gradle
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

// ⚠️ NOTA IMPORTANTE: Debes tener definido HomeViewModel en tu proyecto.
// Esto es solo un placeholder para que compile:
// interface HomeViewModel : ViewModel { ... }
// O usar tu GymClassesViewModel en su lugar y renombrar la instancia.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToReservations: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onClassClick: (GymClass) -> Unit,
    // ⚠️ Se asume que tienes un HomeViewModel.Factory definido
    homeViewModel: HomeViewModel = viewModel()
) {
    val classesState by homeViewModel.classesState.collectAsState()
    val filtersState by homeViewModel.filtersState.collectAsState()

    // 💡 Ejecutar la carga de datos una sola vez
    LaunchedEffect(Unit) {
        // Asumiendo que estas funciones existen en tu HomeViewModel
        homeViewModel.fetchFilters()
        homeViewModel.fetchClasses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RitmoFit") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* No action needed, already in Home */ },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Clases") },
                    label = { Text("Clases") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToReservations,
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Reservas") },
                    label = { Text("Reservas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToQrScanner,
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Escaner QR") },
                    label = { Text("Escaner") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sección de filtros (descomentar cuando esté implementada)
            // FilterSection(homeViewModel, filtersState)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // ⚠️ Se asume que HomeViewModel.ClassesUiState es una sealed class con Loading, Success, Error
                when (val state = classesState) {
                    is HomeViewModel.ClassesUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is HomeViewModel.ClassesUiState.Success -> {
                        if (state.classes.isEmpty()) {
                            Text(text = "No hay clases disponibles para los filtros seleccionados.")
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.classes) { gymClass ->
                                    GymClassItemHome(gymClass = gymClass, onClassClick = onClassClick)
                                }
                            }
                        }
                    }
                    is HomeViewModel.ClassesUiState.Error -> {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                    else -> { /* Estado inicial/vacío */ }
                }
            }
        }
    }
}

// Componente para la lista de clases en Home (unificado)
@Composable
fun GymClassItemHome(gymClass: GymClass, onClassClick: (GymClass) -> Unit) {

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    }

    val classDateFormatted = remember(gymClass.classDate, gymClass.schedule.day) {
        val isoDate = gymClass.classDate
        if (isoDate != null) {
            try {
                // Asume que gymClass.classDate es una fecha ISO (YYYY-MM-DD)
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
            // ✅ CORRECCIÓN APLICADA: Se usa gymClass.name en lugar de gymClass.className
            val className = gymClass.discipline ?: gymClass.className

            Text(
                text = className,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Horario: ${classDateFormatted}, ${gymClass.schedule.startTime} - ${gymClass.schedule.endTime}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Ubicación: ${gymClass.location.name}",
                style = MaterialTheme.typography.bodyMedium
            )

            // ✅ MANEJO SEGURO DE NULOS para profesor (Resuelve el Unresolved reference si el campo puede ser nulo)
            gymClass.professor?.let { professor ->
                Text(
                    text = "Profesor: $professor",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // ✅ MANEJO SEGURO DE NULOS para duración (Resuelve el Unresolved reference si el campo puede ser nulo)
            gymClass.duration?.let { duration ->
                Text(
                    text = "Duración: $duration minutos",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Cupos: ${gymClass.currentCapacity} / ${gymClass.maxCapacity}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Lógica de reserva */ }) {
                Text("Reservar")
            }
        }
    }
}