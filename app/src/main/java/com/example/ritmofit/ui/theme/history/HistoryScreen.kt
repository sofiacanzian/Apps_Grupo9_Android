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
import com.example.ritmofit.data.models.Reservation
// Importamos correctamente las clases de org.threeten.bp para compatibilidad
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

    var showDatePicker by remember { mutableStateOf(false) }
    var pickingStartDate by remember { mutableStateOf(true) }

    // Usamos el formatter de org.threeten.bp
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
                // Usamos org.threeten.bp.LocalDate.format
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
                // Usamos org.threeten.bp.LocalDate.format
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
            // Usamos org.threeten.bp.ZoneId.systemDefault() y org.threeten.bp.Instant
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = (if (pickingStartDate) startDate else endDate)?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedDateMillis = datePickerState.selectedDateMillis
                            if (selectedDateMillis != null) {
                                // Usamos org.threeten.bp.Instant y org.threeten.bp.ZoneId
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
                                reservation = reservation, // Pasamos el objeto Reservation completo
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
    reservation: Reservation,
    onClassClick: () -> Unit
) {
    val gymClass = reservation.classId

    // Formato de fecha para mostrar la fecha real de la clase (classDate)
    val dateFormatter = remember {
        // CAMBIO CLAVE: Formato dd/MM/yyyy
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "ES"))
    }

    // Lógica para parsear la fecha real de la clase (classDate del objeto Reservation)
    val classDateText = remember(reservation.classDate) {
        val isoDate = reservation.classDate // Esta fecha ya viene del esquema Reservation
        if (isoDate != null) {
            try {
                // Parseamos el string de fecha (ej: "2025-09-26T10:00:00.000Z")
                val instant = Instant.parse(isoDate)
                val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
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
            .clickable { onClassClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = gymClass.className,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Muestra la fecha real de la clase (dd/MM/yyyy)
            Text(
                text = "Fecha: $classDateText",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Horario: ${gymClass.schedule.startTime} - ${gymClass.schedule.endTime}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Ubicación: ${gymClass.location.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            // Indicador de estado para el historial
            Text(
                text = "Estado: ${reservation.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
                style = MaterialTheme.typography.bodyMedium,
                color = when (reservation.status) {
                    "attended" -> MaterialTheme.colorScheme.primary
                    "cancelled" -> MaterialTheme.colorScheme.error
                    "expired" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}