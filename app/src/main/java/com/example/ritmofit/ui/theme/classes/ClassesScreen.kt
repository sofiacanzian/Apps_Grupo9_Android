// Archivo: ClassesScreen.kt (CORREGIDO)
package com.example.ritmofit.ui.theme.classes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.ritmofit.network.FilterResponse
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import java.util.Locale
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import com.example.ritmofit.ui.theme.classes.FilterCriteria // Asumiendo que esta es la importación correcta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    onNavigateBack: () -> Unit,
    onClassClick: (GymClass) -> Unit,
    classesViewModel: ClassesViewModel = viewModel(factory = ClassesViewModel.Factory)
) {
    val classesState by classesViewModel.classesState.collectAsState()
    val filterOptions by classesViewModel.filterOptions.collectAsState()
    val currentFilters by classesViewModel.currentFilters.collectAsState()

    Scaffold(
        // topBar ELIMINADO: Soluciona el problema de la doble cabecera "RitmoFit".
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Componente de Filtros
            FilterControls(
                filterOptions = filterOptions,
                currentFilters = currentFilters,
                onFilterSelected = { location, discipline, date ->
                    classesViewModel.setFilter(location, discipline, date)
                },
                onClearFilters = classesViewModel::clearFilters
            )

            // CONTENIDO PRINCIPAL: LISTA DE CLASES
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (val state = classesState) {
                    is ClassesViewModel.ClassesUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is ClassesViewModel.ClassesUiState.Success -> {
                        // Usamos la clase FilterCriteria del ViewModel
                        if (state.classes.isEmpty() && currentFilters == FilterCriteria()) {
                            Text(text = "No hay clases disponibles.")
                        } else if (state.classes.isEmpty() && currentFilters != FilterCriteria()) {
                            Text(text = "No se encontraron clases con los filtros seleccionados.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
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
                    // Manejo del estado inicial si existe
                    else -> { /* Nada */ }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// --- Composable para la UI de Filtros ---
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterControls(
    filterOptions: FilterResponse?,
    currentFilters: FilterCriteria,
    onFilterSelected: (location: String?, discipline: String?, date: String?) -> Unit,
    onClearFilters: () -> Unit
) {
    // Estado local para mostrar el DatePicker
    var showDatePicker by remember { mutableStateOf(false) }

    // Formateador para mostrar la fecha seleccionada en la UI (dd/MM/yyyy)
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    // Formateador para enviar la fecha a la API (YYYY-MM-DD)
    val apiFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Muestra los filtros activos
            val activeFiltersCount = listOfNotNull(
                currentFilters.location,
                currentFilters.discipline,
                currentFilters.date
            ).size

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
                options = filterOptions?.locations ?: emptyList(),
                selectedValue = currentFilters.location,
                onValueSelected = { selectedLocation ->
                    // CORREGIDO (Línea 152 aprox): Se eliminó el argumento con nombre 'location ='
                    onFilterSelected(selectedLocation, null, null)
                },
                modifier = Modifier.weight(1f)
            )

            // 2. Filtro de Disciplina
            FilterDropdown(
                label = "Disciplina",
                options = filterOptions?.disciplines ?: emptyList(),
                selectedValue = currentFilters.discipline,
                onValueSelected = { selectedDiscipline ->
                    // CORREGIDO (Línea 170 aprox): Se eliminó el argumento con nombre 'discipline ='
                    onFilterSelected(null, selectedDiscipline, null)
                },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 3. Filtro de Fecha (en su propia fila para mejor espacio)
        FilterDateButton(
            currentDate = currentFilters.date,
            onDateSelected = { selectedDate ->
                // CORREGIDO (Línea 182 aprox): Se eliminó el argumento con nombre 'date ='
                onFilterSelected(null, null, selectedDate)
            },
            onClear = {
                // CORREGIDO (Línea 185 aprox): Se eliminó el argumento con nombre 'date ='
                onFilterSelected(null, null, null)
            }
        )
    }
    Divider(modifier = Modifier.padding(bottom = 8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDateButton(
    currentDate: String?, // Fecha en formato YYYY-MM-DD (API)
    onDateSelected: (String) -> Unit, // Callback recibe YYYY-MM-DD
    onClear: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // Formateador para mostrar la fecha al usuario
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    // Formateador para pasar a la API (ISO)
    val apiFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    val dateText = remember(currentDate) {
        if (currentDate.isNullOrEmpty()) {
            "Fecha (Todas)"
        } else {
            try {
                // Parsea la fecha de la API y la formatea para mostrar
                val date = LocalDate.parse(currentDate, apiFormatter)
                "Fecha: ${date.format(displayFormatter)}"
            } catch (e: Exception) {
                "Fecha inválida"
            }
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

    if (!currentDate.isNullOrEmpty()) {
        TextButton(onClick = onClear) {
            Text("Quitar Filtro de Fecha")
        }
    }


    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            // Establece la fecha inicial si hay un filtro activo
            initialSelectedDateMillis = currentDate?.let { dateString ->
                try {
                    // Convierte la fecha ISO (YYYY-MM-DD) a milisegundos
                    LocalDate.parse(dateString, apiFormatter)
                        .atStartOfDay(org.threeten.bp.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                } catch (e: Exception) {
                    null
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 1. Obtiene la fecha seleccionada en milisegundos
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            // 2. Convierte a LocalDate (org.threeten.bp.LocalDate)
                            val selectedDate = org.threeten.bp.Instant.ofEpochMilli(selectedDateMillis)
                                .atZone(org.threeten.bp.ZoneId.systemDefault())
                                .toLocalDate()

                            // 3. Formatea al formato API (YYYY-MM-DD)
                            onDateSelected(selectedDate.format(apiFormatter))
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedValue: String?,
    onValueSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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


// --- Componente de Clase (Sin cambios) ---

@Composable
fun GymClassItem(gymClass: GymClass, onClassClick: (GymClass) -> Unit) {
    // Lógica de Formato de Fecha
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    }

    // classDate en el modelo GymClass es String.
    val classDateFormatted = remember(gymClass.classDate, gymClass.schedule.day) {
        try {
            val date = LocalDate.parse(gymClass.classDate)
            "${gymClass.schedule.day.replaceFirstChar { it.titlecase(Locale.getDefault()) }}, ${date.format(dateFormatter)}"
        } catch (e: Exception) {
            // En caso de error de parseo o formato inesperado, muestra solo el día.
            gymClass.schedule.day.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClassClick(gymClass) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val title = gymClass.className

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Horario: usando la fecha formateada (dd/MM/yyyy)
            Text(
                text = "Horario: $classDateFormatted, ${gymClass.schedule.startTime} - ${gymClass.schedule.endTime}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Ubicación: ${gymClass.location.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Profesor: ${gymClass.professor}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Duración: ${gymClass.duration} minutos",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Cupos: ${gymClass.currentCapacity} / ${gymClass.maxCapacity}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}