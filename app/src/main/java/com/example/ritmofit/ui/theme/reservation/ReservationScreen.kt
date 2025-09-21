// Archivo: ReservationsScreen.kt
package com.example.ritmofit.ui.theme.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.data.models.GymClass
import com.example.ritmofit.ui.theme.reservation.ReservationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(
    onNavigateBack: () -> Unit,
    onClassClick: (GymClass) -> Unit,
    reservationsViewModel: ReservationsViewModel = viewModel()
) {
    val reservationsState by reservationsViewModel.reservationsState.collectAsState()

    LaunchedEffect(Unit) {
        reservationsViewModel.fetchUserReservations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reservas") },
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
                                            .clickable { onClassClick(reservation.gymClass) }
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(reservation.gymClass.className, style = MaterialTheme.typography.titleMedium)
                                            Text("Estado: ${reservation.status.displayName}")
                                            Text("Horario: ${reservation.gymClass.schedule.startTime}")
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
}