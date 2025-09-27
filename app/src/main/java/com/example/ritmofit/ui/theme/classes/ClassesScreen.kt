// Archivo: ClassesScreen.kt (Corregido)
package com.example.ritmofit.ui.theme.classes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.network.FilterResponse
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

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

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FilterControls(
                filterOptions = filterOptions,
                currentFilters = currentFilters,
                onFilterSelected = { location, discipline, date ->
                    classesViewModel.setFilter(location, discipline, date)
                },
                onClearFilters = classesViewModel::clearFilters
            )

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
                        if (state.classes.isEmpty() && currentFilters == FilterCriteria()) {
                            Text("No hay clases disponibles.")
                        } else if (state.classes.isEmpty()) {
                            Text("No se encontraron clases con los filtros seleccionados.")
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
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                    else -> Unit
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterControls(
    filterOptions: FilterResponse?,
    currentFilters: FilterCriteria,
    onFilterSelected: (location: String?, discipline: String?, date: String?) -> Unit,
    onClearFilters: () -> Unit
) {
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val apiFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeFiltersCount = listOfNotNull(
                currentFilters.location, currentFilters.discipline, currentFilters.date
            ).size
            Text(
                if (activeFiltersCount > 0) "Filtros Activos ($activeFiltersCount)" else "Filtros",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onClearFilters, enabled = activeFiltersCount > 0) {
                Text("Limpiar Filtros")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterDropdown(
                label = "Ubicación",
                options = filterOptions?.locations ?: emptyList(),
                selectedValue = currentFilters.location,
                onValueSelected = { selected -> onFilterSelected(selected, null, null) },
                modifier = Modifier.weight(1f)
            )

            FilterDropdown(
                label = "Disciplina",
                options = filterOptions?.disciplines ?: emptyList(),
                selectedValue = currentFilters.discipline,
                onValueSelected = { selected -> onFilterSelected(null, selected, null) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        FilterDateButton(
            currentDate = currentFilters.date,
            onDateSelected = { selected -> onFilterSelected(null, null, selected) },
            onClear = { onFilterSelected(null, null, null) }
        )
    }
    Divider(modifier = Modifier.padding(bottom = 8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDateButton(
    currentDate: String?,                 // "yyyy-MM-dd"
    onDateSelected: (String) -> Unit,     // "yyyy-MM-dd"
    onClear: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val apiFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    val dateText = remember(currentDate) {
        if (currentDate.isNullOrEmpty()) "Fecha (Todas)" else runCatching {
            val d = LocalDate.parse(currentDate, apiFormatter)
            "Fecha: ${d.format(displayFormatter)}"
        }.getOrElse { "Fecha inválida" }
    }

    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar Fecha")
        Spacer(Modifier.width(8.dp))
        Text(dateText)
    }

    if (!currentDate.isNullOrEmpty()) {
        TextButton(onClick = onClear) { Text("Quitar Filtro de Fecha") }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDate?.let { iso ->
                runCatching {
                    val d = org.threeten.bp.LocalDate.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE)
                    d.toEpochDay() * 86_400_000L   // 24*60*60*1000
                }.getOrNull()
            }
        )


        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val epochDay = millis / 86_400_000L
                            val selected = org.threeten.bp.LocalDate.ofEpochDay(epochDay)
                            onDateSelected(selected.format(DateTimeFormatter.ISO_LOCAL_DATE)) // "yyyy-MM-dd"
                        }
                        showDatePicker = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
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
                onClick = { onValueSelected(null); expanded = false },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            Divider()
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onValueSelected(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun GymClassItem(gymClass: GymClass, onClassClick: (GymClass) -> Unit) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }
    val classDateFormatted = remember(gymClass.classDate, gymClass.schedule.day) {
        runCatching {
            val d = LocalDate.parse(gymClass.classDate)
            "${gymClass.schedule.day.replaceFirstChar { it.titlecase(Locale.getDefault()) }}, ${d.format(dateFormatter)}"
        }.getOrElse {
            gymClass.schedule.day.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClassClick(gymClass) }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = gymClass.className,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Horario: $classDateFormatted, ${gymClass.schedule.startTime} - ${gymClass.schedule.endTime}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text("Ubicación: ${gymClass.location.name}", style = MaterialTheme.typography.bodyMedium)
            Text("Profesor: ${gymClass.professor}", style = MaterialTheme.typography.bodyMedium)
            Text("Duración: ${gymClass.duration} minutos", style = MaterialTheme.typography.bodyMedium)
            Text("Cupos: ${gymClass.currentCapacity} / ${gymClass.maxCapacity}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
