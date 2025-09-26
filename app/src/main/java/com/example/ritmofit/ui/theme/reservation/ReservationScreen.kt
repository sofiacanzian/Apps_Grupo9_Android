// Archivo: ReservationsScreen.kt
package com.example.ritmofit.ui.theme.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass

@Composable
fun ReservationsScreen(
    onClassClick: (GymClass) -> Unit,
    reservationsViewModel: ReservationsViewModel = viewModel(),
    paddingValues: PaddingValues
) {
    val reservationsState by reservationsViewModel.reservationsState.collectAsState()

    LaunchedEffect(Unit) {
        // Asegura que la lista de reservas se cargue al iniciar
        reservationsViewModel.fetchUserReservations()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        when (val state = reservationsState) {
            is ReservationsViewModel.ReservationsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is ReservationsViewModel.ReservationsUiState.Success -> {
                if (state.reservations.isEmpty()) {
                    Text(text = "No tienes reservas activas.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.reservations) { reservation ->

                            val gymClass = reservation.classId
                            // Hacemos el Card clickable solo si la clase no es nula
                            val isClickable = gymClass != null

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        // ✅ Llama a onClassClick solo si classId no es nulo
                                        .clickable(enabled = isClickable) { gymClass?.let(onClassClick) }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        if (gymClass != null) {
                                            // ✅ CORRECCIÓN: Usamos 'name' en lugar de 'className'
                                            Text(gymClass.className, style = MaterialTheme.typography.titleMedium)
                                            Text("Horario: ${gymClass.schedule.startTime}")
                                        } else {
                                            Text(
                                                text = "Clase Eliminada/No Disponible",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            Text("Horario: Desconocido")
                                        }
                                        Text("Estado: ${reservation.status}")
                                    }
                                    IconButton(
                                        onClick = {
                                            // Llama a la función de cancelación del ViewModel
                                            reservationsViewModel.cancelReservation(reservation.id)
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Cancelar reserva", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is ReservationsViewModel.ReservationsUiState.Error -> {
                Text(text = "Error: ${state.message}")
            }
        }
    }
}