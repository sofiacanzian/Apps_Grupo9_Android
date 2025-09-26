// Archivo: HomeScreen.kt (MODIFICADO Y COMPLETO)
package com.example.ritmofit.home

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuBox

// Imports de Foundation y Layout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

// Imports de Material 3 y Runtime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue // 🔑 CORRECCIÓN 1: AGREGADA LA IMPORTACIÓN SETVALUE
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel

// Imports de Íconos
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.CalendarMonth // ÚNICA INSTANCIA

// Imports de Datos y Modelos
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.network.FilterResponse

// Imports de Fecha (ThreetenBP y Java)
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.ZoneId
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

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

    // Leer los filtros directamente del ViewModel (sin flow, son mutableStateOf)
    val selectedLocation = homeViewModel.selectedLocation
    val selectedDiscipline = homeViewModel.selectedDiscipline
    val selectedDate = homeViewModel.selectedDate

    // Ejecutar la carga de datos una sola vez
    LaunchedEffect(Unit) {
        homeViewModel.fetchFilters()
        homeViewModel.fetchClasses()
    }

    Scaffold(
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
            // ✅ SECCIÓN DE FILTROS AÑADIDA Y DESCOMENTADA
            when (val state = filtersState) {
                is HomeViewModel.FilterUiState.Success -> {
                    FilterSection(
                        filterOptions = state.filters,
                        selectedLocation = selectedLocation,
                        selectedDiscipline = selectedDiscipline,
                        selectedDate = selectedDate,
                        onLocationSelected = homeViewModel::setLocationFilter,
                        onDisciplineSelected = homeViewModel::setDisciplineFilter,
                        onDateSelected = homeViewModel::setDateFilter,
                        onClearFilters = homeViewModel::clearFilters
                    )
                }
                is HomeViewModel.FilterUiState.Loading -> {
                    // Muestra un indicador de carga mientras espera las opciones de filtro
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is HomeViewModel.FilterUiState.Error -> {
                    Text("Error al cargar filtros: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
                else -> {}
            }
            Divider()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Se asume que HomeViewModel.ClassesUiState es una sealed class con Loading, Success, Error
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

// -----------------------------------------------------------------------------
// --- COMPONENTES DE FILTRO ---
// -----------------------------------------------------------------------------

@Composable
fun FilterSection(
    filterOptions: FilterResponse,
    selectedLocation: String?,
    selectedDiscipline: String?,
    selectedDate: Date?,
    onLocationSelected: (String?) -> Unit,
    onDisciplineSelected: (String?) -> Unit,
    onDateSelected: (Date?) -> Unit,
    onClearFilters: () -> Unit
) {
    // Definición de FilterCriteria para contar filtros activos
    val activeFiltersCount = listOfNotNull(selectedLocation, selectedDiscipline, selectedDate).size

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filterText = if (activeFiltersCount > 0) {
                "Filtros Activos ($activeFiltersCount)"
            } else {
                "Filtros"
            }
            Text(filterText, style = MaterialTheme.typography.titleMedium)

            TextButton(
                onClick = onClearFilters,
                enabled = activeFiltersCount > 0
            ) {
                Text("Limpiar Filtros")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Filtro de Ubicación
            FilterDropdown(
                label = "Ubicación",
                options = filterOptions.locations,
                selectedValue = selectedLocation,
                onValueSelected = onLocationSelected,
                modifier = Modifier.weight(1f)
            )

            // 2. Filtro de Disciplina
            FilterDropdown(
                label = "Disciplina",
                options = filterOptions.disciplines,
                selectedValue = selectedDiscipline,
                onValueSelected = onDisciplineSelected,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 3. Filtro de Fecha
        FilterDateButton(
            currentDate = selectedDate,
            onDateSelected = onDateSelected,
            onClear = onClearFilters // Llama a clearFilters general
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedValue: String?,
    onValueSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) } // Línea 261 (Corregida por la importación setValue)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue ?: "Todas",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Opción para seleccionar "Todas" (desactiva el filtro)
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = {
                    onValueSelected(null)
                    expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            Divider()

            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDateButton(
    currentDate: Date?,
    onDateSelected: (Date?) -> Unit,
    onClear: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) } // Línea 314 (Corregida por la importación setValue)

    // Formateador para mostrar la fecha al usuario
    val displayFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val dateText = remember(currentDate) {
        if (currentDate == null) {
            "Fecha (Todas)"
        } else {
            "Fecha: ${displayFormatter.format(currentDate)}"
        }
    }

    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar Fecha")
        Spacer(modifier = Modifier.width(8.dp))
        Text(dateText)
    }

    if (currentDate != null) {
        TextButton(onClick = { onDateSelected(null) }) {
            Text("Quitar Filtro de Fecha")
        }
    }


    if (showDatePicker) {
        // 🔑 CORRECCIÓN 2: Eliminamos la referencia a 'initialSelectedDateMillis' en la inicialización
        val initialMillis = currentDate?.time

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            // Convierte milisegundos de vuelta a java.util.Date
                            onDateSelected(Date(selectedDateMillis))
                        } else {
                            onDateSelected(null)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
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


// --- Componente de Clase (Sin cambios) ---

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

            gymClass.professor?.let { professor ->
                Text(
                    text = "Profesor: $professor",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

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