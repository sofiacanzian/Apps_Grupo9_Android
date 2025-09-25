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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass

@Composable
fun ReservationsScreen(
    onClassClick: (GymClass) -> Unit,
    reservationsViewModel: ReservationsViewModel = viewModel(),
    // Parámetro para recibir el padding del Scaffold superior
    paddingValues: PaddingValues
) {
    val reservationsState by reservationsViewModel.reservationsState.collectAsState()

    LaunchedEffect(Unit) {
        reservationsViewModel.fetchUserReservations()
    }

    // El Scaffold y el TopAppBar fueron eliminados de aquí
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), // <-- Aplica el padding del Scaffold superior aquí
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
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onClassClick(reservation.classId) }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(reservation.classId.name, style = MaterialTheme.typography.titleMedium)
                                        Text("Estado: ${reservation.status}")
                                        Text("Horario: ${reservation.classId.schedule.startTime}")
                                    }
                                    IconButton(
                                        onClick = { reservationsViewModel.cancelReservation(reservation.id) }
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