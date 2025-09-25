// Archivo: HistoryScreen.kt
package com.example.ritmofit.ui.theme.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ritmofit.home.GymClassCard

@Composable
fun HistoryScreen(
    onClassClick: (GymClass) -> Unit,
    historyViewModel: HistoryViewModel = viewModel(),
    // Added parameter to receive padding from the main Scaffold
    paddingValues: PaddingValues
) {
    val historyState by historyViewModel.reservationsState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        historyViewModel.fetchUserReservations()
    }

    // Scaffold and TopAppBar were removed from here to fix the duplication
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) // Apply the padding here
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = historyState) {
            is HistoryViewModel.ReservationsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is HistoryViewModel.ReservationsUiState.Success -> {
                if (state.reservations.isEmpty()) {
                    Text(text = "No tienes reservas en tu historial.")
                } else {
                    LazyColumn {
                        items(state.reservations) { reservation ->
                            GymClassCard(
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