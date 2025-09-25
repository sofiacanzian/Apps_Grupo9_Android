// Archivo: HistoryScreen.kt
package com.example.ritmofit.ui.theme.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.home.GymClassCard // Asumo que esta es la ruta a tu GymClassCard original

// --- CAMBIOS CLAVE AQUÍ: Reemplazar java.time por org.threeten.bp ---
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
// ----------------------------------------------------------------------
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onClassClick: (GymClass) -> Unit,
    historyViewModel: HistoryViewModel = viewModel(),
    paddingValues: PaddingValues
) {
    val historyState by historyViewModel.reservationsState.collectAsState()
    val startDate by historyViewModel.startDate.collectAsState()
    val endDate by historyViewModel.endDate.collectAsState()

    // Estados para controlar el DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    var pickingStartDate by remember { mutableStateOf(true) } // true: inicio, false: fin

    // Formato para mostrar la fecha (Usa org.threeten.bp.format.DateTimeFormatter)
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    LaunchedEffect(key1 = Unit) {
        historyViewModel.fetchUserReservations()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Interfaz de Filtro de Fechas ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campo de Fecha de Inicio
            OutlinedTextField(
                // Usa org.threeten.bp.LocalDate.format
                value = startDate?.format(dateFormatter) ?: "Fecha Inicio",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f),
                label = { Text("Desde") },
                trailingIcon = {
                    IconButton(onClick = {
                        pickingStartDate = true
                        showDatePicker = true
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha inicio")
                    }
                }
            )

            // Campo de Fecha de Fin
            OutlinedTextField(
                // Usa org.threeten.bp.LocalDate.format
                value = endDate?.format(dateFormatter) ?: "Fecha Fin",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f),
                label = { Text("Hasta") },
                trailingIcon = {
                    IconButton(onClick = {
                        pickingStartDate = false
                        showDatePicker = true
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha fin")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botones de Acción (Filtrar y Limpiar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = historyViewModel::applyDateFilter,
                modifier = Modifier.weight(1f)
            ) {
                Text("Filtrar")
            }
            OutlinedButton(
                onClick = historyViewModel::clearFilter,
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpiar Filtro")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // --- Fin Interfaz de Filtro ---

        // --- Diálogo de Selección de Fecha (DatePickerDialog) ---
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                // Usa org.threeten.bp.ZoneId.systemDefault() y org.threeten.bp.Instant
                initialSelectedDateMillis = (if (pickingStartDate) startDate else endDate)?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedDateMillis = datePickerState.selectedDateMillis
                            if (selectedDateMillis != null) {
                                // Usa org.threeten.bp.Instant y org.threeten.bp.ZoneId
                                val selectedDate = Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()

                                if (pickingStartDate) {
                                    historyViewModel.startDate.value = selectedDate
                                } else {
                                    historyViewModel.endDate.value = selectedDate
                                }
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

        // --- Contenido del Historial ---
        when (val state = historyState) {
            is HistoryViewModel.ReservationsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is HistoryViewModel.ReservationsUiState.Success -> {
                if (state.reservations.isEmpty()) {
                    Text(text = "No tienes asistencias en el rango de fechas seleccionado.")
                } else {
                    LazyColumn {
                        items(state.reservations) { reservation ->
                            // Usa el GymClass anidado en la reserva
                            HistoryItemCard(
                                gymClass = reservation.classId,
                                onClassClick = { onClassClick(reservation.classId) }
                            )
                        }
                    }
                }
            }
            is HistoryViewModel.ReservationsUiState.Error -> {
                Text(text = "Error: ${state.message}")
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    gymClass: GymClass,
    onClassClick: (GymClass) -> Unit
) {
    // Formato de fecha para mostrar la fecha real de la clase (classDate)
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale("es", "ES"))
    }

    // Lógica para parsear la fecha real de la clase
    val classDateText = remember(gymClass.classDate) {
        val isoDate = gymClass.classDate
        if (isoDate != null) {
            try {
                val date = LocalDate.parse(isoDate)
                date.format(dateFormatter)
            } catch (e: Exception) {
                "Fecha no disponible"
            }
        } else {
            "Fecha no disponible"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClassClick(gymClass) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = gymClass.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // --- CAMBIO CLAVE: MOSTRAR LA FECHA REAL DE LA RESERVA/CLASE ---
            Text(
                text = "Fecha: $classDateText",
                style = MaterialTheme.typography.bodyMedium
            )
            // -------------------------------------------------------------

            Text(
                text = "Horario: ${gymClass.schedule.startTime} - ${gymClass.schedule.endTime}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Ubicación: ${gymClass.location.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}