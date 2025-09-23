package com.example.ritmofit.home

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.example.ritmofit.R
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToReservations: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onClassClick: (GymClass) -> Unit,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val classesState by homeViewModel.classesState.collectAsState()
    val filtersState by homeViewModel.filtersState.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.fetchFilters()
        homeViewModel.fetchClasses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clases Disponibles") }
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
            // Sección de filtros
            FilterSection(homeViewModel, filtersState)

            // Contenido principal (lista de clases o estado de carga/error)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
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
                                    GymClassCard(
                                        gymClass = gymClass,
                                        onClassClick = { onClassClick(gymClass) }
                                    )
                                }
                            }
                        }
                    }
                    is HomeViewModel.ClassesUiState.Error -> {
                        Text(text = "Error: ${state.message}")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    homeViewModel: HomeViewModel,
    filtersState: HomeViewModel.FilterUiState
) {
    var expandedLocation by remember { mutableStateOf(false) }
    var expandedDiscipline by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (filtersState) {
            is HomeViewModel.FilterUiState.Loading -> {
                CircularProgressIndicator()
            }
            is HomeViewModel.FilterUiState.Success -> {
                // Filtro de Sede
                Box {
                    TextButton(onClick = { expandedLocation = true }) {
                        Text(text = homeViewModel.selectedLocation ?: "Sede")
                    }
                    DropdownMenu(
                        expanded = expandedLocation,
                        onDismissRequest = { expandedLocation = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas") },
                            onClick = {
                                homeViewModel.selectedLocation = null
                                expandedLocation = false
                                homeViewModel.fetchClasses()
                            }
                        )
                        filtersState.filters.locations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location) },
                                onClick = {
                                    homeViewModel.selectedLocation = location
                                    expandedLocation = false
                                    homeViewModel.fetchClasses()
                                }
                            )
                        }
                    }
                }

                // Filtro de Disciplina
                Box {
                    TextButton(onClick = { expandedDiscipline = true }) {
                        Text(text = homeViewModel.selectedDiscipline ?: "Disciplina")
                    }
                    DropdownMenu(
                        expanded = expandedDiscipline,
                        onDismissRequest = { expandedDiscipline = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas") },
                            onClick = {
                                homeViewModel.selectedDiscipline = null
                                expandedDiscipline = false
                                homeViewModel.fetchClasses()
                            }
                        )
                        filtersState.filters.disciplines.forEach { discipline ->
                            DropdownMenuItem(
                                text = { Text(discipline) },
                                onClick = {
                                    homeViewModel.selectedDiscipline = discipline
                                    expandedDiscipline = false
                                    homeViewModel.fetchClasses()
                                }
                            )
                        }
                    }
                }

                // Filtro de Fecha (DatePicker)
                var showDatePicker by remember { mutableStateOf(false) }
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = System.currentTimeMillis()
                )

                TextButton(onClick = { showDatePicker = true }) {
                    val dateText = homeViewModel.selectedDate?.let { date ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        formatter.format(date)
                    } ?: "Fecha"
                    Text(text = dateText)
                }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        homeViewModel.selectedDate = Date(millis)
                                    }
                                    showDatePicker = false
                                    homeViewModel.fetchClasses()
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }
            is HomeViewModel.FilterUiState.Error -> {
                Text(text = "Error al cargar filtros: ${filtersState.message}")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymClassCard(
    gymClass: GymClass,
    onClassClick: (GymClass) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { onClassClick(gymClass) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = gymClass.name, // <-- CORREGIDO: usa 'name' en lugar de 'className'
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Horario: ${gymClass.schedule.day}, ${gymClass.schedule.startTime} - ${gymClass.schedule.endTime}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Ubicación: ${gymClass.location.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Cupos: ${gymClass.currentCapacity} / ${gymClass.maxCapacity}", // <-- CORREGIDO: usa 'currentCapacity'
                style = MaterialTheme.typography.bodyMedium
            )
            gymClass.professor?.let { // <-- CORREGIDO: usa 'professor'
                Text(
                    text = "Profesor: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            gymClass.duration?.let {
                Text(
                    text = "Duración: $it minutos",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}